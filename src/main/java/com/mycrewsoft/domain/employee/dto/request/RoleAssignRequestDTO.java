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
@Schema(description = "역할 배정 요청 DTO")
public class RoleAssignRequestDTO {

    @NotEmpty
    @Schema(description = "배정할 사원 ID 목록", example = "[1, 2, 3]")
    private List<Long> empIds;

    @Schema(
            description = "역할이 적용될 범위 유형 코드. ROLE_EMPLOYEE_SELF는 서버가 SELF로, ROLE_SUPER_ADMIN은 서버가 GLOBAL로 고정하므로 생략할 수 있습니다.",
            example = "DEPT")
    private String scopeTypeCd;

    @Schema(description = "역할이 적용될 범위 ID. ROLE_EMPLOYEE_SELF/ROLE_SUPER_ADMIN 배정 시 서버에서 무시됩니다.", example = "DEPT_024")
    private String scopeId;
}
