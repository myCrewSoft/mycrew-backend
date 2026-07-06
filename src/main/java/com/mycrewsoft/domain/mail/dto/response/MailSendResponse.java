package com.mycrewsoft.domain.mail.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메일 발송 응답")
public class MailSendResponse {

    @Schema(description = "내부 메일 ID", example = "1")
    private Long mailId;

    @Schema(description = "Gmail 메시지 ID", example = "18f00abc123")
    private String externalMessageId;

    @Schema(description = "Gmail 스레드 ID", example = "18f00abc123")
    private String threadId;

    @Schema(description = "발송 일시")
    private LocalDateTime sentAt;
}
