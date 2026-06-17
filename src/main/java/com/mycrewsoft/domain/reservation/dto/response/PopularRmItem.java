package com.mycrewsoft.domain.reservation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PopularRmItem {

    private String confRmNm;
    private int rsrvCount;
}