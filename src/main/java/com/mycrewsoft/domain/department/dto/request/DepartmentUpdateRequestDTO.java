package com.mycrewsoft.domain.department.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "부서 수정 요청")
public class DepartmentUpdateRequestDTO {

    @Schema(description = "상위 부서 코드. 최상위 부서로 변경하려면 생략하거나 null로 전달합니다.", example = "DEPT_001")
    private String parentDeptCd;

    @NotBlank
    @Schema(description = "부서명", example = "플랫폼개발팀")
    private String deptNm;
}
