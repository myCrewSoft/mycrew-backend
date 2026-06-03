package com.mycrewsoft.domain.messenger.service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.messenger.dto.request.CreateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.response.ChatMessageResponse;
import com.mycrewsoft.domain.messenger.dto.response.ChatRoomResponse;
import com.mycrewsoft.domain.messenger.mapper.MsngrDtoMapper;
import com.mycrewsoft.domain.messenger.mapper.MsngrMapper;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmListVO;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmPtcptVO;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmVO;
import com.mycrewsoft.domain.messenger.vo.MsngrMsgDetailVO;
import com.mycrewsoft.domain.messenger.vo.MsngrMsgVO;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MsngrServiceImpl implements MsngrService {

    private final MsngrMapper msngrMapper;
    private final MsngrDtoMapper msngrDtoMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅방 목록 조회
    @Override
    public List<ChatRoomResponse> getChtrmList() {
        // 사용자 확인
        Long empId = getCurrentEmpIdOrThrow();

        // 참여한 채팅방 목록 조회
        List<MsngrChtrmListVO> voList = msngrMapper.selectChtrmListByEmpId(empId);

        // DTO 변환
        return msngrDtoMapper.toRoomResponseListFromList(voList);
    }

    // 채팅방 조회
    @Override
    public ChatRoomResponse getChtrm(Long chtrmId) {
        // 해당 채팅방 조회
        MsngrChtrmVO vo = msngrMapper.selectChtrmById(chtrmId);
        if (vo == null) throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);

        // 사용자 확인
        Long empId = getCurrentEmpIdOrThrow();

        // 본인이 참여자인지 확인
        boolean isParticipant = vo.getMsngrChtrmPtcpt().stream()
                .anyMatch(p -> p.getEmpId().equals(empId));
        if (!isParticipant) throw new CustomException(ErrorCode.CHAT_NOT_PARTICIPANT);

        // dto 변환
        return msngrDtoMapper.toRoomResponse(vo);
    }

    // 메시지 조회
    @Override
    public List<ChatMessageResponse> getMsgList(Long chtrmId) {
        // 해당 채팅방 조회
        MsngrChtrmVO chtrm = msngrMapper.selectChtrmById(chtrmId);
        if (chtrm == null) throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);

        // 사용자 확인
        Long empId = getCurrentEmpIdOrThrow();

        // 본인 참여 정보 조회
        MsngrChtrmPtcptVO myPtcpt = chtrm.getMsngrChtrmPtcpt().stream()
                .filter(p -> p.getEmpId().equals(empId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_PARTICIPANT));

        // 마지막으로 확인한 메시지 ID 조회
        Long lastCfmtnMsgId = myPtcpt.getLastCfmtnMsgId() != null ? myPtcpt.getLastCfmtnMsgId() : 0L;

        // 채팅방 메시지 목록 조회
        List<MsngrMsgDetailVO> msgList = msngrMapper.selectMsgListByChtrmId(chtrmId);

        // mine과 read 데이터 넣어서 반환
        return msgList.stream()
                .map(msgVO -> {
                    ChatMessageResponse response = msngrDtoMapper.toResponseFromDetail(msgVO);
                    response.setMine(msgVO.getSndrId().equals(empId));
                    response.setRead(msgVO.getMsgId() <= lastCfmtnMsgId);
                    return response;
                })
                .toList();
    }

    // 채팅방 생성
    @Override
    @Transactional
    public Long createChtrm(CreateChatRoomRequest request) {
        // 참여자 수에 따라 1:1인지 단체인지 결정
        String chtrmTypeCd = request.getParticipantIds().size() == 1 ? "M1" : "M2";

        // 사용자 확인
        Long empId = getCurrentEmpIdOrThrow();

        // 현재 사용자의 상태값 조회
        String currentSttus = msngrMapper.selectPtcptSttus(empId);

        // 기존 채팅방이 없으면 STS1이 기본값
        if (currentSttus == null) currentSttus = "STS1";
        // dto -> vo 변환
        MsngrChtrmVO chtrmVO = msngrDtoMapper.toChtrmVO(request);
        chtrmVO.setChtrmTypeCd(chtrmTypeCd);
        chtrmVO.setEstblshId(empId);

        // 채팅방 생성
        msngrMapper.insertChtrm(chtrmVO);

        // 본인을 참가자로 생성
        MsngrChtrmPtcptVO myPtcpt = new MsngrChtrmPtcptVO();
        myPtcpt.setChtrmId(chtrmVO.getChtrmId());
        myPtcpt.setEmpId(empId);
        myPtcpt.setPtcptSttusCd("STS4");
        msngrMapper.insertPtcpt(myPtcpt);

        // 요청한 참가자 생성
        for (Long participantId : request.getParticipantIds()) {
            String ptcptSttus = msngrMapper.selectPtcptSttus(participantId);
            if (ptcptSttus == null) ptcptSttus = "STS4"; // 기존 채팅방 없으면 로그아웃 기본값

            MsngrChtrmPtcptVO ptcptVO = new MsngrChtrmPtcptVO();
            ptcptVO.setChtrmId(chtrmVO.getChtrmId());
            ptcptVO.setEmpId(participantId);
            ptcptVO.setPtcptSttusCd(ptcptSttus);
            msngrMapper.insertPtcpt(ptcptVO);
        }

        // 본인의 현재 상태로 전체 채팅방 동기화
        msngrMapper.updatePtcptSttus(empId, currentSttus);

        // 채팅방 ID 반환
        return chtrmVO.getChtrmId();
    }

    // 메시지 저장
    @Override
    @Transactional
    public void saveMsgAndBroadcast(Long chtrmId, String content, Long sndrId) {
        // 채팅방 조회
        MsngrChtrmVO chtrm = msngrMapper.selectChtrmById(chtrmId);
        if (chtrm == null) throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);

        // 본인이 참여자인지 검증
        boolean isParticipant = chtrm.getMsngrChtrmPtcpt().stream()
                .anyMatch(p -> p.getEmpId().equals(sndrId));
        if (!isParticipant) throw new CustomException(ErrorCode.CHAT_NOT_PARTICIPANT);

        // 메시지 생성
        MsngrMsgVO msgVO = new MsngrMsgVO();
        msgVO.setChtrmId(chtrmId);
        msgVO.setSndrId(sndrId);
        msgVO.setMsgCn(content);
        msngrMapper.insertMsg(msgVO);

        MsngrMsgDetailVO savedMsg = msngrMapper.selectMsgById(msgVO.getMsgId());
        
        // vo -> dto 변환
        ChatMessageResponse response = msngrDtoMapper.toResponseFromDetail(savedMsg);
        response.setRead(false);

        // 메시지 전송
        messagingTemplate.convertAndSend("/topic/chats/" + chtrmId, response);
    }

    // 사용자 상태 변경
    @Override
    @Transactional
    public void updatePtcptSttus(String ptcptSttusCd) {
        // 사용자 확인
        Long empId = getCurrentEmpIdOrThrow();

        // 상태 변경
        msngrMapper.updatePtcptSttus(empId, ptcptSttusCd);
    }

    // 사용자 상태 변경 - 리스너용
    @Override
    @Transactional
    public void updatePtcptSttusById(Long empId, String ptcptSttusCd) {
        msngrMapper.updatePtcptSttus(empId, ptcptSttusCd);
    }

    // 마지막 메시지 확인
    @Override
    @Transactional
    public void updateLastCfmtnMsgId(Long chtrmId, Long msgId) {
        // 사용자 확인
        Long empId = getCurrentEmpIdOrThrow();

        // 마지막 메시지 확인
        msngrMapper.updateLastCfmtnMsgId(chtrmId, empId, msgId);
    }

    private Long getCurrentEmpIdOrThrow() {
        Long empId = SecurityUtil.getCurrentEmpId();
        if (empId == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return empId;
    }
}