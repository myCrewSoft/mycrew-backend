package com.mycrewsoft.domain.employee.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "역할 할당 요청 DTO")
public class RoleAssignRequestDTO {

    @NotEmpty
    @Schema(description = "할당할 역할 ID 목록", example = "[1, 2, 3]")
    private List<Long> empIds;

    @NotBlank
    @Schema(description = "역할이 적용될 범위 유형 코드", example = "DEPARTMENT")
    private String scopeTypeCd;

    @Schema(description = "역할이 적용될 범위 ID", example = "DEPT_10")
    private String scopeId;
}
