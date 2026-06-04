package com.mycrewsoft.domain.mail.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "중요 메일 설정 변경 요청")
public class MailImportantUpdateRequest {

    @NotNull(message = "중요 메일 여부는 필수입니다.")
    @Schema(description = "중요 메일 설정 여부", example = "true")
    private Boolean important;
}
