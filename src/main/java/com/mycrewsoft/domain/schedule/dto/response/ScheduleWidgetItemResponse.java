package com.mycrewsoft.domain.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "일정 위젯 항목 응답")
public class ScheduleWidgetItemResponse {

    @Schema(description = "일정 ID")
    private Long id;

    @Schema(description = "일정 구분 코드 (C001:전사, C002:개인, C003:간부, C004:부서, C005:프로젝트, C006:업무)")
    private String scheduleTypeCode;

    @Schema(description = "일정명")
    private String title;

    @Schema(description = "시작 일시")
    private LocalDateTime start;

    @Schema(description = "종료 일시")
    private LocalDateTime end;

    @Schema(description = "종일 일정 여부")
    private Boolean allDay;

    @Schema(description = "부서명 (부서 일정일 때)")
    private String deptNm;

    @Schema(description = "프로젝트명 (프로젝트 일정일 때)")
    private String projNm;

    @Schema(description = "업무명 (업무 일정일 때)")
    private String taskNm;
}