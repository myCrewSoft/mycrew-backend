package com.mycrewsoft.domain.mtng.service;

import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptVO;
import com.mycrewsoft.domain.mtng.vo.MtngVO;
import com.mycrewsoft.domain.reservation.mapper.ReservationMapper;
import com.mycrewsoft.domain.reservation.service.ReservationService;
import com.mycrewsoft.domain.video.mapper.VideoConfMapper;
import com.mycrewsoft.domain.video.vo.VideoConfVO;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

// [템플릿 메서드 패턴]
// 화상회의를 포함하는 회의(ONLINE)의 동작을 담은 클래스.
// HybridMtngCreator가 이 클래스를 상속해서 createMtng, registerParticipants,
// createVideoConfIfNeeded를 그대로 재사용한다.
//
// protected로 선언한 필드(mtngMapper)는 자식 클래스(HybridMtngCreator)에서도
// 접근 가능해야 하기 때문에 private이 아닌 protected로 둔다.
@Component
public class VideoConfMtngCreator extends AbstractMtngCreator {

    protected final MtngMapper mtngMapper;
    protected final VideoConfMapper videoConfMapper;

    public VideoConfMtngCreator(
            ReservationService reservationService,
            MtngMapper mtngMapper,
            VideoConfMapper videoConfMapper,
            ApplicationEventPublisher eventPublisher) {
        // 부모(AbstractMtngCreator)에게 공통 의존성 전달
        super(reservationService, mtngMapper, eventPublisher);
        this.mtngMapper = mtngMapper;
        this.videoConfMapper = videoConfMapper;
    }

    // 1단계: TB_MTNG에 INSERT (모든 타입 공통 로직, 여기서는 ONLINE/HYBRID용으로 구현)
    @Override
    protected Long createMtng(MtngCreateContext context) {
        MtngVO mtngVO = MtngVO.builder()
                .mtngNm(context.getMtngNm())
                .mtngTypeCd(context.getMtngTypeCd().getCode()) // enum -> "MT01"/"MT03" 변환
                .crtrId(context.getCrtrId())
                .beginDt(context.getBeginDt())
                .endDt(context.getEndDt())
                .creatDt(LocalDateTime.now())
                .delYn("N")
                .build();

        mtngMapper.createMtng(mtngVO);
        // createMtng 호출 시 MyBatis <selectKey>가 시퀀스 값을 mtngVO.mtngId에 채워줌
        return mtngVO.getMtngId();
    }

    // 2단계: TB_MTNG_PTCPT에 참여자 INSERT (모든 타입 공통 로직)
    @Override
    protected void registerParticipants(Long mtngId, MtngCreateContext context) {
        List<MtngPtcptVO> ptcptVOList = context.getPtcptEmpIds().stream()
                .map(empId -> MtngPtcptVO.builder()
                        .mtngId(mtngId)
                        .empId(empId)
                        .build())
                .toList();

        mtngMapper.createMtngPtcptList(ptcptVOList);
    }

    // 4단계: TB_VIDEO_CONF에 INSERT (ONLINE/HYBRID만 동작)
    // OfflineMtngCreator는 이 메서드를 오버라이드하지 않으므로
    // 부모(AbstractMtngCreator)의 빈 구현이 호출되어 아무 일도 일어나지 않는다
    @Override
    protected void createVideoConfIfNeeded(Long mtngId, MtngCreateContext context) {
        // roomNm을 먼저 "temp"로 넣고 INSERT해서 VCONF_ID를 채번받는다
        VideoConfVO videoConfVO = VideoConfVO.builder()
                .mtngId(mtngId)
                .roomNm("temp")
                .vconfNm(context.getMtngNm())
                .crtrId(context.getCrtrId())
                .creatDt(LocalDateTime.now())
                .build();

        videoConfMapper.createVideoConf(videoConfVO);

        // 채번된 VCONF_ID로 실제 LiveKit room명을 확정해서 UPDATE
        String roomNm = "meeting-" + videoConfVO.getVconfId();
        videoConfMapper.updateRoomNm(videoConfVO.getVconfId(), roomNm);
    }
}