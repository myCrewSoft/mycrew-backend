package com.mycrewsoft.domain.mail.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "받은편지함 안읽은 메일 수 응답")
public class MailUnreadCountResponse {

    @Schema(description = "안읽은 받은 메일 수", example = "3")
    private final long count;
}
