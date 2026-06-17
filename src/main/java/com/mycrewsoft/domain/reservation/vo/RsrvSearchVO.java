package com.mycrewsoft.domain.reservation.vo;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RsrvSearchVO {

    private LocalDate searchDt;
    private Integer confRmFlr;
    private Long confRmId;
    private String rsrvEmpNm;
    private String intgRsrvYn;
    private String useYn;
}