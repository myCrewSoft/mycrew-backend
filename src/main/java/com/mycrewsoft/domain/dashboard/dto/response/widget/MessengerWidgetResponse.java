package com.mycrewsoft.domain.dashboard.dto.response.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "메신저 위젯 응답")
public class MessengerWidgetResponse {

    @Schema(description = "전체 미읽은 메시지 수")
    private int unreadCount;

    @Schema(description = "채팅방 목록")
    private List<MessengerItem> rooms;

    @Getter
    @Builder
    public static class MessengerItem {

        @Schema(description = "채팅방 ID")
        private Long roomId;

        @Schema(description = "채팅방 이름")
        private String roomName;

        @Schema(description = "마지막 메시지")
        private String lastMessage;

        @Schema(description = "마지막 메시지 시각 (HH:mm)")
        private String lastMessageAt;

        @Schema(description = "미읽은 메시지 수")
        private int unreadCount;
    }
}