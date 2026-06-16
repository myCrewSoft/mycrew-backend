package com.mycrewsoft.domain.mail.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사용자 메일 라벨 생성/수정 요청")
public class MailLabelRequest {

    @NotBlank(message = "라벨명은 필수입니다.")
    @Size(max = 60, message = "라벨명은 60자 이하이어야 합니다.")
    @Schema(description = "라벨명", example = "프로젝트A")
    private String name;
}
