package com.mycrewsoft.domain.mail.dto.request;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "메일 일괄 처리 요청")
public class MailBulkRequest {

    @NotNull(message = "처리 유형은 필수입니다.")
    @Schema(description = "처리 유형: read, unread, trash, important", example = "read")
    private String action;

    @NotEmpty(message = "대상 메일은 1건 이상이어야 합니다.")
    @Schema(description = "대상 내부 메일 ID 목록", example = "[1001, 1002]")
    private List<Long> mailIds = new ArrayList<>();

    @Schema(description = "action=important 일 때 중요 설정 여부", example = "true")
    private Boolean important;
}
