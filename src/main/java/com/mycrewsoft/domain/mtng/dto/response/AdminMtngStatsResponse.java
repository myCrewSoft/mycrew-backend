package com.mycrewsoft.domain.mtng.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminMtngStatsResponse {

    @Schema(description = "오늘 예정된 회의 수")
    private int todayScheduledCnt;

    @Schema(description = "현재 진행중인 회의 수")
    private int inProgressCnt;

    @Schema(description = "완료된 회의 수 (오늘 기준)")
    private int completedCnt;

    @Schema(description = "AI 회의록 생성 대기 중인 회의 수")
    private int aiPendingCnt;
}