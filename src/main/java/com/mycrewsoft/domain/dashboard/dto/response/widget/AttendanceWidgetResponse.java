package com.mycrewsoft.domain.dashboard.dto.response.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "근태 위젯 응답")
public class AttendanceWidgetResponse {

    @Schema(description = "근태 상태 (working/off/absent)")
    private String status;

    @Schema(description = "출근 시각 (HH:mm)")
    private String checkInAt;

    @Schema(description = "퇴근 시각 (HH:mm), 퇴근 전이면 null")
    private String checkOutAt;

    @Schema(description = "근무 시간 (분)")
    private Integer workDurationMinutes;
}