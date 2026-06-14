package com.mycrewsoft.domain.mtng.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;

import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.reservation.dto.request.ReservationCreateRequest;
import com.mycrewsoft.domain.reservation.service.ReservationService;
import com.mycrewsoft.domain.video.event.MeetingInvitedEvent;

import lombok.RequiredArgsConstructor;

// [템플릿 메서드 패턴]
// 회의를 생성하는 "전체 순서"는 항상 같다.
// 하지만 일부 단계(화상회의 생성)는 회의 타입(ONLINE/OFFLINE/HYBRID)마다 다르다.
@RequiredArgsConstructor
public abstract class AbstractMtngCreator {

    private final ReservationService reservationService;
    private final MtngMapper mtngMapper;
    private final ApplicationEventPublisher eventPublisher;

    // final: 이 메서드의 순서는 자식 클래스가 절대 바꿀 수 없다
    public final Long create(MtngCreateContext context) {

        // 0단계: 작성자 본인이 참여자 목록에 없으면 추가 (모든 타입 공통)
        MtngCreateContext normalizedContext = normalizeParticipants(context);

        // 1단계: TB_MTNG row 생성 (모든 타입 공통)
        Long mtngId = createMtng(context);

        // 2단계: TB_MTNG_PTCPT에 참여자 등록 (모든 타입 공통)
        registerParticipants(mtngId, context);

        // 3단계: 회의실 사용 시 TB_CONF_RM_RSRV 생성 + TB_MTNG에 연결 (모든 타입 공통)
        createConfRmRsrvIfNeeded(mtngId, context);

        // 4단계: 화상회의 생성 (ONLINE/HYBRID만 실제 동작, OFFLINE은 아무것도 안 함)
        createVideoConfIfNeeded(mtngId, context);

        // 5단계: 참여자에게 회의 생성/초대 알림 발행 (모든 타입 공통)
        publishInvitedEvent(mtngId, normalizedContext);

        return mtngId;
    }

    // 작성자(crtrId)가 참여자 목록(ptcptEmpIds)에 없으면 추가한 새 Context를 반환
    private MtngCreateContext normalizeParticipants(MtngCreateContext context) {
        List<Long> ptcptEmpIds = new ArrayList<>(context.getPtcptEmpIds());
        if (!ptcptEmpIds.contains(context.getCrtrId())) {
            ptcptEmpIds.add(context.getCrtrId());
        }

        return context.toBuilder()
                .ptcptEmpIds(ptcptEmpIds)
                .build();
    }

    // 이벤트 발행
    private void publishInvitedEvent(Long mtngId, MtngCreateContext context) {
        eventPublisher.publishEvent(
                new MeetingInvitedEvent(
                        mtngId,
                        context.getMtngNm(),
                        context.getBeginDt(),
                        context.getEndDt(),
                        context.getPtcptEmpIds(),
                        context.getCrtrId()
                )
        );
    }
    // ----- 아래는 자식 클래스가 반드시 구현해야 하는 부분 -----

    protected abstract Long createMtng(MtngCreateContext context);

    protected abstract void registerParticipants(Long mtngId, MtngCreateContext context);

    // ----- 아래는 부모가 직접 구현 (모든 타입 공통이라 자식에 위임하지 않음) -----

    // 회의실을 쓰는 경우(confRmId가 있는 경우)에만 TB_CONF_RM_RSRV에 INSERT하고,
    // 생성된 RSRV_ID를 TB_MTNG.CONF_RM_RSRV_ID에 채워넣는다
    private void createConfRmRsrvIfNeeded(Long mtngId, MtngCreateContext context) {
        if (context.getConfRmId() == null) {
            return;
        }

        ReservationCreateRequest reservationRequest = new ReservationCreateRequest(
                context.getConfRmId(),   // roomId
                context.getMtngNm(),     // title
                "N",                     // intgRsrvYn: 회의 연동 예약은 항상 부분예약
                context.getBeginDt(),    // startDateTime
                context.getEndDt()       // endDateTime
        );

        // 중복 예약 체크 + 생성까지 reservationService가 책임짐
        Long rsrvId = reservationService.createReservation(reservationRequest);

        mtngMapper.updateMtngConfRmRsrvId(mtngId, rsrvId);
    }

    // ----- 아래는 기본 동작(아무것도 안 함)을 제공, 필요한 자식만 오버라이드 -----

    protected void createVideoConfIfNeeded(Long mtngId, MtngCreateContext context) {
        // 오프라인 회의는 화상회의가 없으므로 아무것도 하지 않는다
    }
}