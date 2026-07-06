package com.mycrewsoft.domain.dashboard.dto.response.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "알림 위젯 응답")
public class NotificationWidgetResponse {

    @Schema(description = "미읽은 알림 수")
    private int count;

    @Schema(description = "알림 목록")
    private List<NotificationItem> notifications;

    @Getter
    @Builder
    public static class NotificationItem {

        @Schema(description = "알림 ID")
        private Long id;

        @Schema(description = "알림 제목")
        private String title;

        @Schema(description = "알림 내용")
        private String content;

        @Schema(description = "수신 시각")
        private String createdAt;

        @Schema(description = "알림 타입 (schedule/board/approval 등)")
        private String type;
    }
}