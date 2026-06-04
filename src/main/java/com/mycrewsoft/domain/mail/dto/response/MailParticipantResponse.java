package com.mycrewsoft.domain.mail.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "메일 참여자 응답")
public class MailParticipantResponse {

    @Schema(description = "참여자 유형", example = "TO", allowableValues = {
            "FROM", "TO", "CC", "BCC", "REPLY_TO"
    })
    private String type;

    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;
}
