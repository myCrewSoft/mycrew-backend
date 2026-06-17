package com.mycrewsoft.domain.mtng.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminMtngStatsVO {

    private int todayScheduledCnt;
    private int inProgressCnt;
    private int completedCnt;
    private int aiPendingCnt;
}