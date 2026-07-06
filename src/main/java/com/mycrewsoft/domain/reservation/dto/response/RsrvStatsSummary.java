package com.mycrewsoft.domain.reservation.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RsrvStatsSummary {

    private int todayRsrvCount;
    private int weekRsrvCount;
    private int availableRmCount;
    private List<PopularRmItem> popularRmList;
}