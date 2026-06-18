package com.mycrewsoft.domain.dashboard.dto.response.widget;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 대시보드 - 프로젝트 현황 위젯 응답 (전체 프로젝트 상태별 집계)")
public class AdminProjectStatusWidgetResponse {

    @Schema(description = "전체 프로젝트 수", example = "12")
    private int totalCount;

    @Schema(description = "상태별 프로젝트 집계 목록")
    private List<StatusCount> statusCounts;

    @Getter
    @Builder
    @Schema(description = "프로젝트 상태별 집계 항목")
    public static class StatusCount {

        @Schema(description = "프로젝트 상태 코드 (01:예정, 02:진행 중, 03:완료, 04:중단)", example = "02")
        private String statusCode;

        @Schema(description = "프로젝트 상태명", example = "진행 중")
        private String statusName;

        @Schema(description = "해당 상태의 프로젝트 수", example = "3")
        private int count;
    }
}
