package com.mycrewsoft.domain.mail.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "메일 목록 요약 응답")
public class MailSummaryResponse {

    @Schema(description = "내부 메일 ID", example = "1")
    private Long mailId;

    @Schema(description = "Gmail 메시지 ID", example = "18f00abc123")
    private String externalMessageId;

    @Schema(description = "Gmail 스레드 ID", example = "18f00abc123")
    private String threadId;

    @Schema(description = "메일 제목", example = "회의 자료 공유")
    private String subject;

    @Schema(description = "Gmail 스니펫")
    private String snippet;

    @Schema(description = "발신자 이메일", example = "sender@example.com")
    private String fromEmail;

    @Schema(description = "수신자 요약", example = "user@example.com 외 2명")
    private String toSummary;

    @Schema(description = "발송 일시")
    private LocalDateTime sentAt;

    @Schema(description = "읽지 않음 여부", example = "true")
    private boolean unread;

    @Schema(description = "중요 메일 여부", example = "false")
    private boolean important;

    @Schema(description = "첨부파일 보유 여부", example = "true")
    private boolean hasAttachment;

    @Schema(description = "메일함 라벨 목록", example = "[\"INBOX\", \"UNREAD\"]")
    private List<String> mailboxTypes = new ArrayList<>();
}
