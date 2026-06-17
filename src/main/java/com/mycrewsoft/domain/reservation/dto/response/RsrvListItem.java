package com.mycrewsoft.domain.reservation.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RsrvListItem {

    private Long rsrvId;
    private Long confRmId;
    private String confRmNm;
    private int confRmFlr;
    private String confRmHo;
    private String rsrvPurps;
    private String rsrvEmpNm;
    private LocalDateTime beginDt;
    private LocalDateTime endDt;
    private String intgRsrvYn;
    private String delYn;
}