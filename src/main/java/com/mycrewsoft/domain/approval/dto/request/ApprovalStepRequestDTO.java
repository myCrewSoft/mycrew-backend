package com.mycrewsoft.domain.approval.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "결재 단계 요청")
public class ApprovalStepRequestDTO {

    @Schema(description = "결재 방식 코드", example = "01")
    @NotBlank(message = "결재 방식 코드는 필수입니다.")
    @Size(max = 2, message = "결재 방식 코드는 2자 이하이어야 합니다.")
    private String aprvlMthdCd;

    @Schema(description = "결재 순서", example = "1")
    @NotNull(message = "결재 순서는 필수입니다.")
    @Positive(message = "결재 순서는 1 이상이어야 합니다.")
    private Long aprvlOrd;

    @Schema(description = "결재자 사원 ID 목록", example = "[101, 102]")
    @NotEmpty(message = "결재자는 최소 1명 이상 필요합니다.")
    private List<@NotNull(message = "결재자 사원 ID는 null일 수 없습니다.") Long> aprvrEmpIds;
}
