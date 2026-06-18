package com.mycrewsoft.domain.mtng.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminMtngAnalyticsResponse {

    @Schema(description = "회의 유형별 비율 (도넛 차트용)")
    private List<AdminMtngAnalyticsItemResponse> typeStats;

    @Schema(description = "월별 회의 건수 추이 (막대 차트용, 최근 6개월)")
    private List<AdminMtngAnalyticsItemResponse> monthlyStats;

    @Schema(description = "시간대별 회의 집중도 (히트맵용, 09~18시)")
    private List<AdminMtngAnalyticsItemResponse> hourlyStats;

    @Schema(description = "회의록 생성률 (0~100 사이 정수)")
    private int momGenerationRate;
}