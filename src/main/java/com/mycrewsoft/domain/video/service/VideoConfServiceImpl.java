package com.mycrewsoft.domain.video.service;

import com.mycrewsoft.ai.chatbot.service.PromptService;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.mtng.enums.MtngSttus;
import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.mtng.service.MtngMomService;
import com.mycrewsoft.domain.mtng.vo.MtngDetailVO;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptDetailVO;
import com.mycrewsoft.domain.video.config.LiveKitTokenProvider;
import com.mycrewsoft.domain.video.dto.response.VideoTokenResponse;
import com.mycrewsoft.domain.video.event.MeetingEndedEvent;
import com.mycrewsoft.domain.video.mapper.VideoConfMapper;
import com.mycrewsoft.domain.video.vo.VideoChatLogVO;
import com.mycrewsoft.domain.video.vo.VideoPtcptLogVO;
import com.mycrewsoft.domain.video.vo.VideoRcrdgVO;
import com.mycrewsoft.security.util.SecurityUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoConfServiceImpl implements VideoConfService {

    private final VideoConfMapper videoConfMapper;
    private final MtngMapper mtngMapper;
    private final MtngMomService mtngMomService;
    private final LiveKitTokenProvider liveKitTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    // Spring AI ChatClient - AI 모델 직접 호출용 (스트리밍 아닌 단건 응답)
    // /chat/stream 엔드포인트를 거치지 않고 서버 내부에서 직접 호출한다
    private final ChatClient chatClient;

    // promptMeetingService - 회의록 전용 프롬프트 빌더
    // @RequiredArgsConstructor와 @Qualifier는 함께 쓸 수 없어 @Autowired로 별도 주입
    @Autowired
    @Qualifier("promptMeetingService")
    private PromptService promptMeetingService;

    @Override
    @Transactional
    public VideoTokenResponse issueToken(Long vconfId) {
        Long empId = SecurityUtil.getCurrentEmpId();

        MtngDetailVO detailVO = mtngMapper.selectMtngDetailByVconfId(vconfId);
        if (detailVO == null) {
            throw new CustomException(ErrorCode.VIDEO_CONF_NOT_FOUND);
        }

        // 종료된 회의는 토큰 발급 불가 (시간 기준 계산)
        MtngSttus sttus = MtngSttus.of(detailVO.getBeginDt(), detailVO.getEndDt(), LocalDateTime.now());
        if (sttus == MtngSttus.ENDED) {
            throw new CustomException(ErrorCode.VIDEO_ALREADY_ENDED);
        }

        // 참여자 권한 확인 + 현재 사원 정보 조회 (토큰 메타데이터 구성에 사용)
        List<MtngPtcptDetailVO> ptcptList =
                mtngMapper.selectMtngPtcptDetailList(detailVO.getMtngId());
        MtngPtcptDetailVO currentPtcpt = ptcptList.stream()
                .filter(p -> p.getEmpId().equals(empId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.VIDEO_ACCESS_DENIED));

        // 입장마다 새 row 생성 (1인 N행, 나갔다 들어오는 경우 대비)
        VideoPtcptLogVO logVO = VideoPtcptLogVO.builder()
                .vconfId(vconfId)
                .empId(empId)
                .build();
        videoConfMapper.insertPtcptLog(logVO);

        String token = liveKitTokenProvider.createToken(
                detailVO.getRoomNm(),
                String.valueOf(empId),
                currentPtcpt.getEmpNm(),
                currentPtcpt.getDeptNm(),
                currentPtcpt.getJobGrdNm(),
                currentPtcpt.getPrflImgFileId()
        );
        return new VideoTokenResponse(detailVO.getRoomNm(), token);
    }

    @Override
    @Transactional
    public void leaveConf(Long vconfId) {
        Long empId = SecurityUtil.getCurrentEmpId();

        // 가장 최근의 미퇴장(LEAV_DT IS NULL) 입장 로그를 찾아 퇴장 시각 기록
        Long ptcptLogId = videoConfMapper.selectLatestOpenPtcptLogId(vconfId, empId);
        if (ptcptLogId != null) {
            videoConfMapper.updatePtcptLogLeavDt(ptcptLogId);
        }
        // ptcptLogId가 null이면 이미 퇴장 처리됐거나 입장 기록 없음 -> 조용히 무시
    }

    @Override
    @Transactional
    public void endConf(Long vconfId) {
        Long empId = SecurityUtil.getCurrentEmpId();

        MtngDetailVO detailVO = mtngMapper.selectMtngDetailByVconfId(vconfId);
        if (detailVO == null) {
            throw new CustomException(ErrorCode.VIDEO_CONF_NOT_FOUND);
        }

        // 호스트(회의 작성자)만 종료 가능
        if (!detailVO.getCrtrId().equals(empId)) {
            throw new CustomException(ErrorCode.VIDEO_ACCESS_DENIED);
        }

        // 참여자 목록을 먼저 조회 (buildReference와 이벤트 발행에서 공통 사용)
        List<MtngPtcptDetailVO> ptcptList =
                mtngMapper.selectMtngPtcptDetailList(detailVO.getMtngId());

        List<Long> ptcptEmpIds = ptcptList.stream()
            .map(MtngPtcptDetailVO::getEmpId)
            .toList();

        eventPublisher.publishEvent(
            new MeetingEndedEvent(
                    detailVO.getMtngNm(),
                    ptcptEmpIds,
                    vconfId,
                    detailVO.getMtngId()
            )
        );
    }

    @Override
    @Transactional
    public void saveRcrdg(Long vconfId, Long atchFileId) {
        Long empId = SecurityUtil.getCurrentEmpId();

        MtngDetailVO detailVO = mtngMapper.selectMtngDetailByVconfId(vconfId);
        if (detailVO == null) {
            throw new CustomException(ErrorCode.VIDEO_CONF_NOT_FOUND);
        }

        // 호스트만 녹취록 저장 가능
        if (!detailVO.getCrtrId().equals(empId)) {
            throw new CustomException(ErrorCode.VIDEO_ACCESS_DENIED);
        }

        VideoRcrdgVO rcrdgVO = VideoRcrdgVO.builder()
                .vconfId(vconfId)
                .rcrdgAtchFileId(atchFileId)
                .build();
        videoConfMapper.insertRcrdg(rcrdgVO);
    }

    // AI 프롬프트의 reference를 구성한다
    // 회의 기본정보(회의명, 일시, 장소, 주재자, 참석자)와
    // TB_VIDEO_CHAT_LOG에 누적된 STT 대화로그를 하나의 텍스트로 합친다
    private String buildReference(Long vconfId, MtngDetailVO detailVO,
            List<MtngPtcptDetailVO> ptcptList) {

        // STT 청크가 CREAT_DT 순으로 쌓인 대화로그 전체 조회
        List<VideoChatLogVO> chatLogs =
                videoConfMapper.selectChatLogsByVconfId(vconfId);

        String ptcptNames = ptcptList.stream()
                .map(MtngPtcptDetailVO::getEmpNm)
                .collect(Collectors.joining(", "));

        // [직원ID] 발언내용 형태로 각 청크를 줄 단위로 연결
        // AI가 서명란 인원수를 결정하려면 참석자/주재자 정보가 여기에 있어야 한다
        String logs = chatLogs.stream()
                .map(log -> "[" + log.getMbrId() + "] " + log.getSpkngCn())
                .collect(Collectors.joining("\n"));

        return """
                회의명: %s
                일시: %s ~ %s
                장소: %s
                주재자: %s
                참석자: %s

                [대화 로그]
                %s
                """.formatted(
                detailVO.getMtngNm(),
                detailVO.getBeginDt(),
                detailVO.getEndDt(),
                detailVO.getConfRmNm() != null ? detailVO.getConfRmNm() : "온라인",
                detailVO.getCrtrNm(),
                ptcptNames,
                logs
        );
    }
}