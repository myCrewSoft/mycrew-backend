package com.mycrewsoft.domain.mail.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "메일 일괄 처리 결과")
public class MailBulkResponse {

    @Schema(description = "처리 성공 건수", example = "5")
    private final int processed;

    @Schema(description = "처리 실패 건수", example = "0")
    private final int failed;
}
