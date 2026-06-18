package com.mycrewsoft.domain.reservation.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationDetailVO {

	private Long rsrvId;
    private Long confRmId;
    private Long rsrvEmpId;
    private String rsrvEmpNm;
    private String rsrvEmpDeptCd;
    private String rsrvEmpJobGrdCd;
    private Long rsrvEmpPrflImgFileId;
    private String rsrvPurps;
    private LocalDateTime beginDt;
    private LocalDateTime endDt;
    private String delYn;
    private String allDayYn;

    private Long mtngId;
}