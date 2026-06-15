package com.mycrewsoft.domain.department.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "사원 부서 이동 요청")
public class DepartmentMemberTransferRequestDTO {

    @NotBlank
    @Schema(description = "이동 대상 부서 코드", example = "DEPT_002")
    private String targetDeptCd;

    @NotEmpty
    @Schema(description = "이동할 사원 ID 목록", example = "[1001, 1002]")
    private List<Long> empIds;
}
