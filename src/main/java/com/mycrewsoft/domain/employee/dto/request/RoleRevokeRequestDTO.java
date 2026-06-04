package com.mycrewsoft.domain.employee.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "역할 해제 요청 DTO")
public class RoleRevokeRequestDTO {

    @NotEmpty
    @Schema(description = "역할이 해제될 직원 ID 목록", example = "[1, 2, 3]")
    private List<Long> empIds;

    @Schema(description = "역할이 해제될 범위 유형 코드", example = "DEPARTMENT")
    private String scopeTypeCd;

    @Schema(description = "역할이 해제될 범위 ID", example = "DEPT_10")
    private String scopeId;
}
