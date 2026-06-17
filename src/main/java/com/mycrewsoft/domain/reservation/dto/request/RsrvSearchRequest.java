package com.mycrewsoft.domain.reservation.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RsrvSearchRequest {

	private String searchDt;
    private Integer confRmFlr;
    private Long confRmId;
    private String rsrvEmpNm;
    private String intgRsrvYn;
    private String useYn;

}
