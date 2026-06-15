package com.mycrewsoft.domain.video.service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.mtng.enums.MtngSttus;
import com.mycrewsoft.domain.mtng.event.MeetingEndedEvent;
import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.mtng.service.MtngMomService;
import com.mycrewsoft.domain.mtng.vo.MtngDetailVO;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptDetailVO;
import com.mycrewsoft.domain.video.config.LiveKitTokenProvider;
import com.mycrewsoft.domain.video.dto.response.VideoTokenResponse;
import com.mycrewsoft.domain.video.mapper.VideoConfMapper;
import com.mycrewsoft.domain.video.vo.VideoPtcptLogVO;
import com.mycrewsoft.domain.video.vo.VideoRcrdgVO;
import com.mycrewsoft.security.util.SecurityUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // chatClient, promptMeetingService 제거
    // → MeetingEndedEventListener로 이동

    @Override
    @Transactional
    public VideoTokenResponse issueToken(Long vconfId) {
        Long empId = SecurityUtil.getCurrentEmpId();

        MtngDetailVO detailVO = mtngMapper.selectMtngDetailByVconfId(vconfId);
        if (detailVO == null) {
            throw new CustomException(ErrorCode.VIDEO_CONF_NOT_FOUND);
        }

        MtngSttus sttus = MtngSttus.of(detailVO.getBeginDt(), detailVO.getEndDt(), LocalDateTime.now());
        if (sttus == MtngSttus.ENDED) {
            throw new CustomException(ErrorCode.VIDEO_ALREADY_ENDED);
        }

        List<MtngPtcptDetailVO> ptcptList =
                mtngMapper.selectMtngPtcptDetailList(detailVO.getMtngId());
        MtngPtcptDetailVO currentPtcpt = ptcptList.stream()
                .filter(p -> p.getEmpId().equals(empId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.VIDEO_ACCESS_DENIED));

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

        Long ptcptLogId = videoConfMapper.selectLatestOpenPtcptLogId(vconfId, empId);
        if (ptcptLogId != null) {
            videoConfMapper.updatePtcptLogLeavDt(ptcptLogId);
        }
    }

    @Override
    @Transactional
    public void endConf(Long vconfId) {
        Long empId = SecurityUtil.getCurrentEmpId();

        MtngDetailVO detailVO = mtngMapper.selectMtngDetailByVconfId(vconfId);
        if (detailVO == null) {
            throw new CustomException(ErrorCode.VIDEO_CONF_NOT_FOUND);
        }

        if (!detailVO.getCrtrId().equals(empId)) {
            throw new CustomException(ErrorCode.VIDEO_ACCESS_DENIED);
        }

        List<MtngPtcptDetailVO> ptcptList =
                mtngMapper.selectMtngPtcptDetailList(detailVO.getMtngId());

        List<Long> ptcptEmpIds = ptcptList.stream()
                .map(MtngPtcptDetailVO::getEmpId)
                .toList();

        // 이벤트 발행 후 즉시 응답
        // AI 회의록 생성은 MeetingEndedEventListener가 @Async로 처리
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

        if (!detailVO.getCrtrId().equals(empId)) {
            throw new CustomException(ErrorCode.VIDEO_ACCESS_DENIED);
        }

        VideoRcrdgVO rcrdgVO = VideoRcrdgVO.builder()
                .vconfId(vconfId)
                .rcrdgAtchFileId(atchFileId)
                .build();
        videoConfMapper.insertRcrdg(rcrdgVO);
    }
}