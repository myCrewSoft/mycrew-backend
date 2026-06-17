package com.mycrewsoft.domain.reservation.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RsrvSummaryVO {

    private int todayRsrvCount;
    private int weekRsrvCount;
    private int availableRmCount;
}