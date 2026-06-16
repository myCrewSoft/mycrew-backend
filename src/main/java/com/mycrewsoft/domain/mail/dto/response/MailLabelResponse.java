package com.mycrewsoft.domain.mail.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사용자 메일 라벨 응답")
public class MailLabelResponse {

    @Schema(description = "라벨 ID", example = "5001")
    private Long labelId;

    @Schema(description = "라벨명", example = "프로젝트A")
    private String name;
}
