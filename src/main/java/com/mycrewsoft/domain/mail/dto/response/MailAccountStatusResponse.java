package com.mycrewsoft.domain.mail.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메일 계정 연동 상태 응답")
public class MailAccountStatusResponse {

    @Schema(description = "메일 계정 연동 여부", example = "true")
    private boolean accountLinked;

    @Schema(description = "화면 표시용 계정 상태", example = "TOKEN_INVALID", allowableValues = {
            "NONE", "ACTIVE", "TOKEN_INVALID", "REVOKED"
    })
    private String status;

    @Schema(description = "연동된 메일 주소", example = "user@example.com")
    private String emailAddr;

    @Schema(description = "메일 토큰 상태 코드", example = "INVALID")
    private String tokenStatusCd;

    @Schema(description = "다시 연동 버튼 표시 필요 여부", example = "true")
    private boolean reconnectRequired;
}
