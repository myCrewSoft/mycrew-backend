package com.mycrewsoft.domain.mtng.service;

import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptVO;
import com.mycrewsoft.domain.mtng.vo.MtngVO;
import com.mycrewsoft.domain.reservation.mapper.ReservationMapper;
import com.mycrewsoft.domain.reservation.service.ReservationService;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OfflineMtngCreator extends AbstractMtngCreator {

    private final MtngMapper mtngMapper;

    public OfflineMtngCreator(
            ReservationService reservationService,
            MtngMapper mtngMapper,
            ApplicationEventPublisher eventPublisher) {
        super(reservationService, mtngMapper, eventPublisher);
        this.mtngMapper = mtngMapper;
    }

    // 1단계: TB_MTNG에 INSERT
    @Override
    protected Long createMtng(MtngCreateContext context) {
        MtngVO mtngVO = MtngVO.builder()
                .mtngNm(context.getMtngNm())
                .mtngTypeCd(context.getMtngTypeCd().getCode())
                .crtrId(context.getCrtrId())
                .beginDt(context.getBeginDt())
                .endDt(context.getEndDt())
                .creatDt(LocalDateTime.now())
                .delYn("N")
                .build();

        mtngMapper.createMtng(mtngVO);
        return mtngVO.getMtngId();
    }

    // 2단계: TB_MTNG_PTCPT에 참여자 INSERT
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
}