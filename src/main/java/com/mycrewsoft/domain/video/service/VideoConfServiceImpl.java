package com.mycrewsoft.domain.video.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.video.config.LiveKitTokenProvider;
import com.mycrewsoft.domain.video.dto.request.VideoConfCreateRequest;
import com.mycrewsoft.domain.video.dto.request.VideoMomAprvlRequest;
import com.mycrewsoft.domain.video.dto.request.VideoMomUpdateRequest;
import com.mycrewsoft.domain.video.dto.response.VideoConfResponse;
import com.mycrewsoft.domain.video.dto.response.VideoMomResponse;
import com.mycrewsoft.domain.video.dto.response.VideoTokenResponse;
import com.mycrewsoft.domain.video.event.MeetingEndedEvent;
import com.mycrewsoft.domain.video.event.MeetingInvitedEvent;
import com.mycrewsoft.domain.video.mapper.VideoConfDtoMapper;
import com.mycrewsoft.domain.video.mapper.VideoConfMapper;
import com.mycrewsoft.domain.video.vo.VideoConfListVO;
import com.mycrewsoft.domain.video.vo.VideoConfVO;
import com.mycrewsoft.domain.video.vo.VideoMomAprvlVO;
import com.mycrewsoft.domain.video.vo.VideoMomHistVO;
import com.mycrewsoft.domain.video.vo.VideoMomVO;
import com.mycrewsoft.domain.video.vo.VideoPtcptDetailVO;
import com.mycrewsoft.domain.video.vo.VideoPtcptVO;
import com.mycrewsoft.domain.video.vo.VideoRcrdgVO;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoConfServiceImpl implements VideoConfService {

    private final VideoConfMapper videoConfMapper;
    private final VideoConfDtoMapper videoConfDtoMapper;
    private final LiveKitTokenProvider liveKitTokenProvider;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public VideoConfResponse createConf(VideoConfCreateRequest request) {
        Long empId = SecurityUtil.getCurrentEmpId();

        // DTO → VO 변환
        VideoConfVO confVO = videoConfDtoMapper.toConfVO(request);
        confVO.setCrtrId(empId);
        confVO.setRoomNm("temp");

        videoConfMapper.insertConf(confVO);

        // 채번된 vconfId로 roomNm 확정
        confVO.setRoomNm("meeting-" + confVO.getVconfId());
        videoConfMapper.updateRoomNm(confVO.getVconfId(), confVO.getRoomNm());

        // 생성자 본인이 없으면 참여자 목록에 추가
        List<Long> ptcptEmpIds = new ArrayList<>(request.getPtcptEmpIds());
        if (!ptcptEmpIds.contains(empId)) {
            ptcptEmpIds.add(empId);
        }

        if (!ptcptEmpIds.isEmpty()) {
            ptcptEmpIds.stream()
                    .map(id -> {
                        VideoPtcptVO vo = new VideoPtcptVO();
                        vo.setVconfId(confVO.getVconfId());
                        vo.setEmpId(id);
                        return vo;
                    })
                    .forEach(videoConfMapper::insertPtcpt);
        }

        VideoConfListVO saved = videoConfMapper.selectConfById(confVO.getVconfId());
        
        eventPublisher.publishEvent(
        	new MeetingInvitedEvent(saved.getVconfNm(), ptcptEmpIds)
        );
        
        return videoConfDtoMapper.toConfResponse(saved);
    }

    @Override
    public List<VideoConfResponse> getConfList() {
        Long empId = SecurityUtil.getCurrentEmpId();
        List<VideoConfListVO> list = videoConfMapper.selectConfList(empId);
        return videoConfDtoMapper.toConfResponseList(list);
    }

    @Override
    public VideoConfResponse getConf(Long vconfId) {
    	VideoConfListVO vo = videoConfMapper.selectConfById(vconfId);
        if (vo == null) throw new CustomException(ErrorCode.VIDEO_CONF_NOT_FOUND);

        Long empId = SecurityUtil.getCurrentEmpId();
        boolean isPtcpt = vo.getVideoPtcpt().stream()
                .anyMatch(p -> p.getEmpId().equals(empId));
        if (!isPtcpt) throw new CustomException(ErrorCode.VIDEO_ACCESS_DENIED);

        return videoConfDtoMapper.toConfResponse(vo);
    }

    @Override
    @Transactional
    public VideoTokenResponse issueToken(Long vconfId) {
        Long empId = SecurityUtil.getCurrentEmpId();

        VideoConfListVO vo = videoConfMapper.selectConfById(vconfId);
        if (vo == null) throw new CustomException(ErrorCode.VIDEO_CONF_NOT_FOUND);

        // 종료된 회의는 토큰 발급 불가
        if ("03".equals(vo.getConfSttusCd())) {
            throw new CustomException(ErrorCode.VIDEO_ALREADY_ENDED);
        }

        // 참여자 목록에서 본인 행 조회
        VideoPtcptDetailVO myPtcpt = vo.getVideoPtcpt().stream()
                .filter(p -> p.getEmpId().equals(empId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.VIDEO_ACCESS_DENIED));

        // 입장 시각 업데이트
        videoConfMapper.updatePtcptJoinDt(myPtcpt.getVconfPtcptId());

        String token = liveKitTokenProvider.createToken(vo.getRoomNm(), String.valueOf(empId));
        return new VideoTokenResponse(vo.getRoomNm(), token);
    }

    @Override
    @Transactional
    public void endConf(Long vconfId) {
        Long empId = SecurityUtil.getCurrentEmpId();

        VideoConfListVO vo = videoConfMapper.selectConfById(vconfId);
        if (vo == null) throw new CustomException(ErrorCode.VIDEO_CONF_NOT_FOUND);

        // 호스트만 종료 가능
        if (!vo.getCrtrId().equals(empId)) {
            throw new CustomException(ErrorCode.VIDEO_ACCESS_DENIED);
        }
        
        videoConfMapper.updateConfSttus(vconfId, "03");
        
        // 참여자 목록에서 empId 추출
        List<Long> ptcptEmpIds = vo.getVideoPtcpt().stream()
                .map(VideoPtcptDetailVO::getEmpId)
                .toList();
        
        eventPublisher.publishEvent(
            	new MeetingEndedEvent(vo.getVconfNm(), ptcptEmpIds)
        );
    }

    @Override
    public VideoMomResponse getMom(Long vconfId) {
        Long empId = SecurityUtil.getCurrentEmpId();

        VideoConfListVO confVO = videoConfMapper.selectConfById(vconfId);
        if (confVO == null) throw new CustomException(ErrorCode.VIDEO_CONF_NOT_FOUND);

        boolean isPtcpt = confVO.getVideoPtcpt().stream()
                .anyMatch(p -> p.getEmpId().equals(empId));
        if (!isPtcpt) throw new CustomException(ErrorCode.VIDEO_ACCESS_DENIED);

        VideoMomVO momVO = videoConfMapper.selectMomByVconfId(vconfId);
        if (momVO == null) throw new CustomException(ErrorCode.VIDEO_MOM_NOT_FOUND);
        return videoConfDtoMapper.toMomResponse(momVO);
    }

    @Override
    @Transactional
    public VideoMomResponse updateMom(Long vconfId, VideoMomUpdateRequest request) {
        Long empId = SecurityUtil.getCurrentEmpId();

        VideoMomVO momVO = videoConfMapper.selectMomByVconfId(vconfId);
        if (momVO == null) throw new CustomException(ErrorCode.VIDEO_MOM_NOT_FOUND);

        // 확정된 회의록은 수정 불가
        if ("04".equals(momVO.getMomSttusCd())) {
            throw new CustomException(ErrorCode.VIDEO_MOM_ALREADY_CONFIRMED);
        }

        // 수정 전 내용 이력 저장
        VideoMomHistVO histVO = new VideoMomHistVO();
        histVO.setMomId(momVO.getMomId());
        histVO.setMomCn(momVO.getMomCn());
        histVO.setEdtrId(empId);
        videoConfMapper.insertMomHist(histVO);

        // DTO → VO 변환 후 세팅
        VideoMomVO updateVO = videoConfDtoMapper.toMomVO(request);
        updateVO.setMomId(momVO.getMomId());
        updateVO.setMomSttusCd("02");
        updateVO.setEdtrId(empId);
        videoConfMapper.updateMom(updateVO);

        VideoMomVO updated = videoConfMapper.selectMomByVconfId(vconfId);
        return videoConfDtoMapper.toMomResponse(updated);
    }

    @Override
    @Transactional
    public void requestMomReview(Long vconfId) {
        Long empId = SecurityUtil.getCurrentEmpId();

        VideoMomVO momVO = videoConfMapper.selectMomByVconfId(vconfId);
        if (momVO == null) throw new CustomException(ErrorCode.VIDEO_MOM_NOT_FOUND);

        // 담당자만 검토 요청 가능
        if (!momVO.getEdtrId().equals(empId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 확정된 회의록은 검토 요청 불가
        if ("04".equals(momVO.getMomSttusCd())) {
            throw new CustomException(ErrorCode.VIDEO_MOM_ALREADY_CONFIRMED);
        }

        int nextRound = momVO.getAprvlRoundNo() + 1;
        VideoConfListVO confVO = videoConfMapper.selectConfById(vconfId);

        List<VideoMomAprvlVO> aprvlList = confVO.getVideoPtcpt().stream()
                .map(ptcpt -> {
                    VideoMomAprvlVO vo = new VideoMomAprvlVO();
                    vo.setMomId(momVO.getMomId());
                    vo.setPtcptId(ptcpt.getVconfPtcptId());
                    vo.setAprvlRoundNo(nextRound);
                    return vo;
                })
                .toList();

        videoConfMapper.insertAprvlList(aprvlList);

        // 회의록 상태 → 검토 중, 회차 업데이트
        momVO.setMomSttusCd("03");
        momVO.setAprvlRoundNo(nextRound);
        videoConfMapper.updateMom(momVO);
    }

    @Override
    @Transactional
    public void approveMom(Long vconfId, VideoMomAprvlRequest request) {
        Long empId = SecurityUtil.getCurrentEmpId();

        VideoMomVO momVO = videoConfMapper.selectMomByVconfId(vconfId);
        if (momVO == null) throw new CustomException(ErrorCode.VIDEO_MOM_NOT_FOUND);

        // 현재 회차에서 본인 결재 행 조회
        List<VideoMomAprvlVO> aprvlList = videoConfMapper.selectAprvlListByMomId(momVO.getMomId());
        VideoMomAprvlVO myAprvl = aprvlList.stream()
                .filter(a -> a.getAprvlRoundNo().equals(momVO.getAprvlRoundNo())
                        && a.getPtcptId().equals(empId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.VIDEO_ACCESS_DENIED));

        // 이미 결재 처리된 경우 차단
        if (!"01".equals(myAprvl.getAprvlSttusCd())) {
            throw new CustomException(ErrorCode.VIDEO_APRVL_ALREADY_DONE);
        }

        myAprvl.setAprvlSttusCd(request.getAprvlSttusCd());
        videoConfMapper.updateAprvl(myAprvl);

        // 전원 승인 시 회의록 자동 확정
        boolean allApproved = aprvlList.stream()
                .filter(a -> a.getAprvlRoundNo().equals(momVO.getAprvlRoundNo()))
                .allMatch(a -> "02".equals(
                        a.getAprvlId().equals(myAprvl.getAprvlId())
                                ? request.getAprvlSttusCd()
                                : a.getAprvlSttusCd()));

        if (allApproved) {
            momVO.setMomSttusCd("04");
            videoConfMapper.updateMom(momVO);
        }
    }

    @Override
    @Transactional
    public void saveRcrdg(Long vconfId, Long atchFileId) {
        Long empId = SecurityUtil.getCurrentEmpId();

        VideoConfListVO confVO = videoConfMapper.selectConfById(vconfId);
        if (confVO == null) throw new CustomException(ErrorCode.VIDEO_CONF_NOT_FOUND);

        // 호스트만 녹취록 저장 가능
        if (!confVO.getCrtrId().equals(empId)) {
            throw new CustomException(ErrorCode.VIDEO_ACCESS_DENIED);
        }

        VideoRcrdgVO rcrdgVO = new VideoRcrdgVO();
        rcrdgVO.setVconfId(vconfId);
        rcrdgVO.setRcrdgAtchFileId(atchFileId);
        videoConfMapper.insertRcrdg(rcrdgVO);
    }
}