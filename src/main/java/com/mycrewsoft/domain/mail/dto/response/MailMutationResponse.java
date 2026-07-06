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
@Schema(description = "메일 상태 변경 응답")
public class MailMutationResponse {

    @Schema(description = "내부 메일 ID", example = "1")
    private Long mailId;

    @Schema(description = "변경 상태", example = "READ", allowableValues = {
            "TRASHED", "READ", "IMPORTANT_UPDATED", "RESTORED", "DELETED"
    })
    private String status;
}
