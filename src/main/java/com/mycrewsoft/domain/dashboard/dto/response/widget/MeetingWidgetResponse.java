package com.mycrewsoft.domain.dashboard.dto.response.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "오늘 회의 위젯 응답")
public class MeetingWidgetResponse {

    @Schema(description = "오늘 회의 목록")
    private List<MeetingItem> meetings;

    @Getter
    @Builder
    public static class MeetingItem {

        @Schema(description = "회의 ID")
        private Long id;

        @Schema(description = "회의 제목")
        private String title;

        @Schema(description = "시작 시각 (HH:mm)")
        private String startAt;

        @Schema(description = "종료 시각 (HH:mm)")
        private String endAt;

        @Schema(description = "장소")
        private String location;
    }
}