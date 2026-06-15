package com.mycrewsoft.domain.mtng.service;

import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.reservation.mapper.ReservationMapper;
import com.mycrewsoft.domain.reservation.service.ReservationService;
import com.mycrewsoft.domain.video.mapper.VideoConfMapper;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

// [템플릿 메서드 패턴 + 상속을 통한 중복 제거]
// HYBRID(화상회의 + 회의실)는 VideoConfMtngCreator(ONLINE)와
// createMtng / registerParticipants / createVideoConfIfNeeded가 완전히 동일하다.
//
// 회의실 연결(createConfRmRsrvIfNeeded)은 AbstractMtngCreator가
// context.getConfRmId() 값 유무로 자동 처리하므로,
// HYBRID만을 위한 별도 코드는 필요 없다.
//
// 따라서 VideoConfMtngCreator를 상속만 하고 본문은 비워둔다.
// 추후 HYBRID에만 필요한 동작(예: 회의실 영상장비 자동 설정)이 생기면
// 이 클래스에서 필요한 메서드만 오버라이드하면 된다.
@Component
public class HybridMtngCreator extends VideoConfMtngCreator {

    public HybridMtngCreator(
            ReservationService reservationService,
            MtngMapper mtngMapper,
            VideoConfMapper videoConfMapper,
            ApplicationEventPublisher eventPublisher) {
        super(reservationService, mtngMapper, videoConfMapper, eventPublisher);
    }
}