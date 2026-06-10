package com.mycrewsoft.domain.approval.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "결재 처리 가능 여부 응답")
public class ApprovalAvailabilityResponse {

    @Schema(description = "기안문 일련번호", example = "1001")
    private Long drftDocSn;

    @Schema(description = "처리 가능 여부", example = "true")
    private boolean available;

    @Schema(description = "처리 가능 여부 설명", example = "현재 결재 단계의 대기 중인 결재자입니다.")
    private String message;
}
