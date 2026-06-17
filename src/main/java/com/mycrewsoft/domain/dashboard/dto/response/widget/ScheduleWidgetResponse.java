package com.mycrewsoft.domain.dashboard.dto.response.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "오늘 일정 위젯 응답")
public class ScheduleWidgetResponse {

    @Schema(description = "오늘 일정 목록")
    private List<ScheduleItem> schedules;

    @Getter
    @Builder
    public static class ScheduleItem {

        @Schema(description = "일정 ID")
        private Long id;

        @Schema(description = "일정 제목")
        private String title;

        @Schema(description = "일정 구분 코드 (C001:전사, C002:개인, C003:간부, C004:부서, C005:프로젝트, C006:업무)")
        private String scheduleTypeCode;

        @Schema(description = "시작 시각 (HH:mm)")
        private String startAt;

        @Schema(description = "종료 시각 (HH:mm)")
        private String endAt;

        @Schema(description = "부서명 (부서 일정일 때)")
        private String deptNm;

        @Schema(description = "프로젝트명 (프로젝트 일정일 때)")
        private String projNm;

        @Schema(description = "업무명 (업무 일정일 때)")
        private String taskNm;
    }
}