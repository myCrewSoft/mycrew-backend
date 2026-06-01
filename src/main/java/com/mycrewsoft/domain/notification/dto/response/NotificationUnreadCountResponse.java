package com.mycrewsoft.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "미읽음 알림 개수 응답 DTO")
public class NotificationUnreadCountResponse {

    @Schema(description = "미읽음 알림 개수", example = "5")
    private long unreadCount;
    
}
