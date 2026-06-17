package com.mycrewsoft.domain.dashboard.dto.response.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "메일 위젯 응답")
public class MailWidgetResponse {

    @Schema(description = "미읽은 메일 수")
    private int unreadCount;

    @Schema(description = "최신 메일 목록 (5건)")
    private List<MailItem> mails;

    @Getter
    @Builder
    public static class MailItem {

        @Schema(description = "메일 ID")
        private Long id;

        @Schema(description = "발신자 이름")
        private String senderName;

        @Schema(description = "제목")
        private String subject;

        @Schema(description = "수신 시각")
        private String receivedAt;

        @Schema(description = "읽음 여부")
        private boolean isRead;
    }
}