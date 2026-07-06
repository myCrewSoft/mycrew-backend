package com.mycrewsoft.domain.messenger.service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.domain.messenger.dto.request.AddParticipantsRequest;
import com.mycrewsoft.domain.messenger.dto.request.CreateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.request.RemoveParticipantsRequest;
import com.mycrewsoft.domain.messenger.dto.request.UpdateChatRoomRequest;
import com.mycrewsoft.domain.messenger.dto.response.ChatEventResponse;
import com.mycrewsoft.domain.messenger.dto.response.ChatMessageResponse;
import com.mycrewsoft.domain.messenger.dto.response.ChatParticipantResponse;
import com.mycrewsoft.domain.messenger.dto.response.ChatRoomResponse;
import com.mycrewsoft.domain.messenger.dto.response.ParticipantChangedResponse;
import com.mycrewsoft.domain.messenger.dto.response.ParticipantStatusResponse;
import com.mycrewsoft.domain.messenger.dto.response.ReadChangedResponse;
import com.mycrewsoft.domain.messenger.enums.ChatEventType;
import com.mycrewsoft.domain.messenger.enums.ParticipantStatus;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MsngrServiceImpl implements MsngrService {

    private final MsngrMapper msngrMapper;
    private final MsngrDtoMapper msngrDtoMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmployeeMapper employeeMapper;

    private static final int WIDGET_MSNGR_LIMIT = 3;
    
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
        validateActiveParticipant(vo, empId);

        // dto 변환
        ChatRoomResponse response = msngrDtoMapper.toRoomResponse(vo);
        List<ChatParticipantResponse> participants = toParticipantResponses(vo.getMsngrChtrmPtcpt());

        response = response.toBuilder()
                .participantCount(participants.size())
                .participants(participants)
                .build();

        return applyOneToOneSummary(response, empId);
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
        MsngrChtrmPtcptVO myPtcpt = getActiveParticipant(chtrm, empId);

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
        ParticipantStatus currentStatus = ParticipantStatus.fromCodeOrDefault(
                msngrMapper.selectPtcptSttus(empId),
                ParticipantStatus.ONLINE
        );
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
        myPtcpt.setPtcptSttusCd(ParticipantStatus.OFFLINE.getCode());
        msngrMapper.insertPtcpt(myPtcpt);

        // 요청한 참가자 생성
        for (Long participantId : request.getParticipantIds()) {
            ParticipantStatus participantStatus = ParticipantStatus.fromCodeOrDefault(
                    msngrMapper.selectPtcptSttus(participantId),
                    ParticipantStatus.OFFLINE
            );

            MsngrChtrmPtcptVO ptcptVO = new MsngrChtrmPtcptVO();
            ptcptVO.setChtrmId(chtrmVO.getChtrmId());
            ptcptVO.setEmpId(participantId);
            ptcptVO.setPtcptSttusCd(participantStatus.getCode());
            msngrMapper.insertPtcpt(ptcptVO);
        }

        // 본인의 현재 상태로 전체 채팅방 동기화
        msngrMapper.updatePtcptSttus(empId, currentStatus.getCode());

        // 채팅방 ID 반환
        return chtrmVO.getChtrmId();
    }

    // 채팅방 수정
    @Override
    @Transactional
    public void updateChtrm(Long chtrmId, UpdateChatRoomRequest request) {
        // 개설자 확인
        Long empId = getCurrentEmpIdOrThrow();
        MsngrChtrmVO chtrm = msngrMapper.selectChtrmById(chtrmId);
        if (chtrm == null) throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        Long estblsgId = chtrm.getEstblshId();
        if(!empId.equals(estblsgId)) throw new CustomException(ErrorCode.ACCESS_DENIED);

        // dto -> vo 변환
        MsngrChtrmVO chtrmVO = msngrDtoMapper.toChtrmVO(request);

        // 채팅방 정보 수정
        int result = msngrMapper.updateChtrm(chtrmId, chtrmVO);
        if(result == 0) throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
    }

    // 채팅방 삭제
    @Override
    @Transactional
    public void deleteChtrm(Long chtrmId) {
        // 개설자 확인
        Long empId = getCurrentEmpIdOrThrow();
        MsngrChtrmVO chtrm = msngrMapper.selectChtrmById(chtrmId);
        if (chtrm == null) throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);

        if (!empId.equals(chtrm.getEstblshId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 채팅방 정보 수정
        int result = msngrMapper.deleteChtrm(chtrmId);
        if(result == 0) throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
    }

    // 참여자 추가
    @Override
    @Transactional
    public void addChtrmPtcpt(Long chtrmId, AddParticipantsRequest request) {
        // 본인이 해당 방의 참가자인지 확인
        Long empId = getCurrentEmpIdOrThrow();
        
        MsngrChtrmVO chtrm = msngrMapper.selectChtrmById(chtrmId);
        if (chtrm == null) throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);

        validateActiveParticipant(chtrm, empId);

        // 단체 방인지 확인
        if("M1".equals(chtrm.getChtrmTypeCd())) throw new CustomException(ErrorCode.CHAT_DIRECT_ROOM_CANNOT_ADD_PARTICIPANT);

        // 실제로 추가된 사용자 목록
        List<Long> addedParticipantIds = new ArrayList<>();

        for (Long participantId : request.getParticipantIds()) {
            if (isActiveParticipant(chtrm, participantId)) continue;

            // 참여자 상태 확인
            ParticipantStatus participantStatus = ParticipantStatus.fromCodeOrDefault(
                    msngrMapper.selectPtcptSttus(participantId),
                    ParticipantStatus.OFFLINE
            );

            MsngrChtrmPtcptVO existingPtcpt = msngrMapper.selectPtcptByChtrmIdAndEmpId(chtrmId, participantId);
            if (existingPtcpt == null) {
                MsngrChtrmPtcptVO ptcptVO = new MsngrChtrmPtcptVO();
                ptcptVO.setChtrmId(chtrmId);
                ptcptVO.setEmpId(participantId);
                ptcptVO.setPtcptSttusCd(participantStatus.getCode());
                msngrMapper.insertPtcpt(ptcptVO);
            } else {
                msngrMapper.rejoinPtcpt(chtrmId, participantId, participantStatus.getCode());
            }

            // 참여자 목록 추가
            addedParticipantIds.add(participantId);
        }

        // 실제로 추가된 참여자 추가 이벤트 호출
        if (!addedParticipantIds.isEmpty()) {
            sendParticipantChangedEvent(
                chtrmId,
                addedParticipantIds,
                ChatEventType.PARTICIPANT_ADDED
            );
        }
    }

    // 참여자 제거
    @Override
    @Transactional
    public void removeChtrmPtcpt(Long chtrmId, RemoveParticipantsRequest request) {
        // 개설자 확인
        Long empId = getCurrentEmpIdOrThrow();
        MsngrChtrmVO chtrm = msngrMapper.selectChtrmById(chtrmId);
        if (chtrm == null) throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);

        if (!empId.equals(chtrm.getEstblshId())) throw new CustomException(ErrorCode.ACCESS_DENIED);

        // 제거하려는 참여자가 개설자인지 확인
        if (request.getParticipantIds().contains(chtrm.getEstblshId())) throw new CustomException(ErrorCode.CHAT_OWNER_CANNOT_BE_REMOVED);

        // 실제로 삭제된 사용자 목록
        List<Long> removedParticipantIds = new ArrayList<>();

        // 선택한 참여자 제거
        for (Long participantId : request.getParticipantIds()) {
            int result = msngrMapper.leavePtcpt(chtrmId, participantId);
            if (result == 0) throw new CustomException(ErrorCode.CHAT_NOT_PARTICIPANT);
            // 삭제 리스트에 추가
            removedParticipantIds.add(participantId);
        }

        // 실제로 삭제된 참여자 삭제 이벤트 호출
        if (!removedParticipantIds.isEmpty()) {
            sendParticipantChangedEvent(
                    chtrmId,
                    removedParticipantIds,
                    ChatEventType.PARTICIPANT_REMOVED
            );
        }
    }

    // 사용자 상태 변경
    @Override
    @Transactional
    public void updatePtcptSttus(ParticipantStatus status) {
        // 사용자 확인
        Long empId = getCurrentEmpIdOrThrow();

        updateParticipantStatus(empId, status);
    }

    // 사용자 상태 변경 - 리스너용
    @Override
    @Transactional
    public void updatePtcptSttusById(Long empId, ParticipantStatus status) {
        updateParticipantStatus(empId, status);
    }

    // 마지막 메시지 확인
    @Override
    @Transactional
    public void updateLastCfmtnMsgId(Long chtrmId, Long msgId) {
        // 참여자 확인
        Long empId = getCurrentEmpIdOrThrow();

        MsngrChtrmVO chtrm = msngrMapper.selectChtrmById(chtrmId);
        if (chtrm == null) throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);

        validateActiveParticipant(chtrm, empId);

        // 마지막 메시지 확인
        msngrMapper.updateLastCfmtnMsgId(chtrmId, empId, msgId);

        // 안 읽은 사람 수 확인
        int unreadCount = msngrMapper.selectUnreadCountByMsgId(chtrmId, msgId);

        // 안 읽은 메시지
        ReadChangedResponse response = ReadChangedResponse.builder()
                .chatRoomId(chtrmId)
                .empId(empId)
                .messageId(msgId)
                .lastCfmtnMsgId(msgId)
                .unreadCount(unreadCount)
                .build();

        // 웹소켓 송신
        ChatEventResponse<ReadChangedResponse> event = ChatEventResponse
                .<ReadChangedResponse>builder()
                .eventType(ChatEventType.READ_CHANGED)
                .data(response)
                .build();

        messagingTemplate.convertAndSend("/topic/chats/" + chtrmId + "/events", event);
    }

    // 메시지 저장
    @Override
    @Transactional
    public void saveMsgAndBroadcast(Long chtrmId, String content, Long sndrId) {
        // 채팅방 조회
        MsngrChtrmVO chtrm = msngrMapper.selectChtrmById(chtrmId);
        if (chtrm == null) throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);

        // 본인이 참여자인지 검증
        validateActiveParticipant(chtrm, sndrId);

        // 메시지 생성
        MsngrMsgVO msgVO = new MsngrMsgVO();
        msgVO.setChtrmId(chtrmId);
        msgVO.setSndrId(sndrId);
        msgVO.setMsgCn(content);
        msngrMapper.insertMsg(msgVO);
        msngrMapper.updateLastCfmtnMsgId(chtrmId, sndrId, msgVO.getMsgId());

        MsngrMsgDetailVO savedMsg = msngrMapper.selectMsgById(msgVO.getMsgId());
        
        // vo -> dto 변환
        ChatMessageResponse response = msngrDtoMapper.toResponseFromDetail(savedMsg);
        response.setRead(false);

        // 메시지 전송
        ChatEventResponse<ChatMessageResponse> event = ChatEventResponse
                .<ChatMessageResponse>builder()
                .eventType(ChatEventType.MESSAGE_CREATED)
                .data(response)
                .build();

        messagingTemplate.convertAndSend("/topic/chats/" + chtrmId + "/events", event);
    }
    
    // 프로젝트 채팅방 생성
    @Override
    @Transactional
    public Long createProjectChtrm(
        Long projId,
        String projNm,
        Long crtrId,
        List<Long> empIds
    ) {

        // 1. 채팅방 생성
        MsngrChtrmVO chtrmVO = new MsngrChtrmVO();
        chtrmVO.setChtrmNm(projNm);
        chtrmVO.setChtrmExpln(projNm + "프로젝트 채팅방");
        chtrmVO.setChtrmTypeCd("M3");
        chtrmVO.setEstblshId(crtrId);

        msngrMapper.insertChtrm(chtrmVO);

        Long chtrmId = chtrmVO.getChtrmId();

        // 2. 프로젝트 참여자들을 채팅방 참여자로 등록
        for (Long empId : empIds) {
            MsngrChtrmPtcptVO ptcptVO = new MsngrChtrmPtcptVO();
            ptcptVO.setChtrmId(chtrmId);
            ptcptVO.setEmpId(empId);
            ptcptVO.setPtcptSttusCd(ParticipantStatus.OFFLINE.getCode());

            msngrMapper.insertPtcpt(ptcptVO);
        }
        
        return chtrmId;
    }
    
    // 프로젝트 인원 추가시 초대
    @Override
    @Transactional
    public void addProjectChtrmParticipants(Long chtrmId, List<Long> empIds) {
        if (chtrmId == null) {
            throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        if (empIds == null || empIds.isEmpty()) {
            return;
        }

        List<Long> distinctEmpIds = empIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        for (Long empId : distinctEmpIds) {
            ParticipantStatus participantStatus = ParticipantStatus.fromCodeOrDefault(
                    msngrMapper.selectPtcptSttus(empId),
                    ParticipantStatus.OFFLINE
            );

            MsngrChtrmPtcptVO ptcptVO = new MsngrChtrmPtcptVO();
            ptcptVO.setChtrmId(chtrmId);
            ptcptVO.setEmpId(empId);
            ptcptVO.setPtcptSttusCd(participantStatus.getCode());

            msngrMapper.mergeProjectPtcpt(ptcptVO);
        }
    }
    
    // 프로젝트 인원 퇴출 시 퇴장 조치
    @Override
    @Transactional
    public void removeProjectChtrmParticipant(Long chtrmId, Long empId) {
        if (chtrmId == null) {
            throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        if (empId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        int result = msngrMapper.updateProjectPtcptLeaveDt(chtrmId, empId);

        if (result == 0) {
            throw new CustomException(ErrorCode.CHAT_NOT_PARTICIPANT);
        }
    }
    
    
    @Override
    public List<ChatRoomResponse> getChtrmListForWidget() {
        Long empId = getCurrentEmpIdOrThrow();
        List<MsngrChtrmListVO> voList = msngrMapper.selectUnreadChtrmListByEmpId(empId, WIDGET_MSNGR_LIMIT);
        return msngrDtoMapper.toRoomResponseListFromList(voList);
    }
    
    // 사용자 확인
    private Long getCurrentEmpIdOrThrow() {
        Long empId = SecurityUtil.getCurrentEmpId();
        if (empId == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return empId;
    }

    private MsngrChtrmPtcptVO getActiveParticipant(MsngrChtrmVO chtrm, Long empId) {
        return chtrm.getMsngrChtrmPtcpt().stream()
                .filter(p -> empId.equals(p.getEmpId()) && p.getLeavDt() == null)
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_PARTICIPANT));
    }

    private boolean isActiveParticipant(MsngrChtrmVO chtrm, Long empId) {
        return chtrm.getMsngrChtrmPtcpt().stream()
                .anyMatch(p -> empId.equals(p.getEmpId()) && p.getLeavDt() == null);
    }

    private void validateActiveParticipant(MsngrChtrmVO chtrm, Long empId) {
        getActiveParticipant(chtrm, empId);
    }

    private List<ChatParticipantResponse> toParticipantResponses(List<MsngrChtrmPtcptVO> ptcptList) {
        if (ptcptList == null) {
            return List.of();
        }

        return ptcptList.stream()
                .map(this::toParticipantResponse)
                .toList();
    }

    private ChatParticipantResponse toParticipantResponse(MsngrChtrmPtcptVO ptcpt) {
        EmployeeProfileDTO profile = employeeMapper.selectEmployeeProfileByEmpId(ptcpt.getEmpId());

        return ChatParticipantResponse.builder()
                .empId(ptcpt.getEmpId())
                .empNm(profile != null ? profile.getEmpNm() : null)
                .deptNm(getDeptNm(profile))
                .jobGrdNm(getJobGrdNm(profile))
                .prflImgFileId(profile != null ? profile.getPrflImgFileId() : null)
                .ptcptSttusCd(ptcpt.getPtcptSttusCd())
                .build();
    }

    private String getDeptNm(EmployeeProfileDTO profile) {
        if (profile == null || profile.getDepartment() == null) {
            return null;
        }
        return profile.getDepartment().getDeptNm();
    }

    private String getJobGrdNm(EmployeeProfileDTO profile) {
        if (profile == null || profile.getJobGrade() == null) {
            return null;
        }
        return profile.getJobGrade().getJobGrdNm();
    }

    private ChatRoomResponse applyOneToOneSummary(ChatRoomResponse response, Long empId) {
        if (!"M1".equals(response.getType()) || response.getParticipants() == null) {
            return response;
        }

        ChatParticipantResponse other = response.getParticipants().stream()
                .filter(participant -> !empId.equals(participant.getEmpId()))
                .findFirst()
                .orElse(null);

        if (other == null) {
            return response;
        }
        
        if(response.getName().isEmpty()) {
        	return response.toBuilder()
                    .name(other.getEmpNm())
                    .prflImgFileId(other.getPrflImgFileId())
                    .status(other.getPtcptSttusCd())
                    .jobTitle(other.getJobGrdNm())
                    .department(other.getDeptNm())
                    .build();
        } else {
        	return response.toBuilder()
                    .prflImgFileId(other.getPrflImgFileId())
                    .status(other.getPtcptSttusCd())
                    .jobTitle(other.getJobGrdNm())
                    .department(other.getDeptNm())
                    .build();
        }
    }

    // 참여자 변경 웹소켓 송신
    private void sendParticipantChangedEvent(
        Long chtrmId,
        List<Long> participantIds,
        ChatEventType eventType
    ) {
        ParticipantChangedResponse response = ParticipantChangedResponse.builder()
                .chatRoomId(chtrmId)
                .participantIds(participantIds)
                .build();

        ChatEventResponse<ParticipantChangedResponse> event =
                ChatEventResponse.<ParticipantChangedResponse>builder()
                        .eventType(eventType)
                        .data(response)
                        .build();

        messagingTemplate.convertAndSend(
                "/topic/chats/" + chtrmId + "/events",
                event
        );
    }

    private void updateParticipantStatus(Long empId, ParticipantStatus status) {
        if (status == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        msngrMapper.updatePtcptSttus(empId, status.getCode());
        sendParticipantStatusChangedEvent(empId, status);
    }

    private void sendParticipantStatusChangedEvent(Long empId, ParticipantStatus status) {
        ParticipantStatusResponse response = ParticipantStatusResponse.builder()
                .empId(empId)
                .ptcptSttusCd(status.getCode())
                .build();

        ChatEventResponse<ParticipantStatusResponse> event =
                ChatEventResponse.<ParticipantStatusResponse>builder()
                        .eventType(ChatEventType.PARTICIPANT_STATUS_CHANGED)
                        .data(response)
                        .build();

        messagingTemplate.convertAndSend("/topic/chats/status", event);

        msngrMapper.selectActiveChtrmIdsByEmpId(empId).forEach(chtrmId ->
                messagingTemplate.convertAndSend(
                        "/topic/chats/" + chtrmId + "/events",
                        event
                )
        );
    }
}
