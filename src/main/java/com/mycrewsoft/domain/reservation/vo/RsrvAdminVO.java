package com.mycrewsoft.domain.reservation.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RsrvAdminVO {

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