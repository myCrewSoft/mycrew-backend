package com.mycrewsoft.domain.mtng.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminMtngAnalyticsItemResponse {

    @Schema(description = "항목 레이블 (유형명, 월, 시간대 등 차트마다 다름)")
    private String label;

    @Schema(description = "해당 항목의 회의 건수")
    private int cnt;
}