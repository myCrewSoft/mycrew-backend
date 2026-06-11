package com.mycrewsoft.domain.approval.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "결재 처리 사유 요청")
public class ApprovalActionRequestDTO {

    @Schema(description = "승인 또는 반려 사유", example = "내용 확인했습니다.")
    @Size(max = 4000, message = "처리 사유는 4000자 이하이어야 합니다.")
    private String reason;
}
