package com.mycrewsoft.domain.dashboard.dto.response.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "회의실 예약 위젯 응답")
public class ReservationWidgetResponse {

    @Schema(description = "오늘 예약 목록")
    private List<ReservationItem> reservations;

    @Getter
    @Builder
    public static class ReservationItem {

        @Schema(description = "예약 ID")
        private Long id;

        @Schema(description = "회의실 이름")
        private String resourceName;

        @Schema(description = "시작 시각 (HH:mm)")
        private String startAt;

        @Schema(description = "종료 시각 (HH:mm)")
        private String endAt;

        @Schema(description = "예약 상태 (confirmed/waiting)")
        private String status;
    }
}