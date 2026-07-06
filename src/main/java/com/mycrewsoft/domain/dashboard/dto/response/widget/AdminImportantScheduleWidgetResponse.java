package com.mycrewsoft.domain.dashboard.dto.response.widget;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 대시보드 - 중요 일정 위젯 응답 (전사·간부 일정 통합)")
public class AdminImportantScheduleWidgetResponse {

    @Schema(description = "중요 일정 목록 (오늘 + 다가오는 전사/간부 일정)")
    private List<ScheduleItem> schedules;

    @Getter
    @Builder
    @Schema(description = "중요 일정 항목")
    public static class ScheduleItem {

        @Schema(description = "일정 ID", example = "501")
        private Long id;

        @Schema(description = "일정 제목", example = "2026년 상반기 전사 워크숍")
        private String title;

        @Schema(description = "일정 구분 코드 (C001:전사, C003:간부)", example = "C001")
        private String scheduleTypeCode;

        @Schema(description = "일정 구분명", example = "전사")
        private String scheduleTypeName;

        @Schema(description = "시작 일시 (yyyy-MM-dd HH:mm)", example = "2026-06-20 09:00")
        private String startAt;

        @Schema(description = "종료 일시 (yyyy-MM-dd HH:mm)", example = "2026-06-20 18:00")
        private String endAt;

        @Schema(description = "종일 일정 여부", example = "false")
        private boolean allDay;
    }
}
