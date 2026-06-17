package com.mycrewsoft.domain.room.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfRmSummaryVO {

    private int totalCount;
    private int inUseCount;
    private int availableCount;
    private double avgOccupancyRate;
}