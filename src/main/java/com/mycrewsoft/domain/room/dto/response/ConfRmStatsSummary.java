package com.mycrewsoft.domain.room.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConfRmStatsSummary {

 private int totalCount;
 private int inUseCount;
 private int availableCount;
 private double avgOccupancyRate;
}