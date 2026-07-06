package com.mycrewsoft.domain.department.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "부서 생성 요청")
public class DepartmentCreateRequestDTO {

    @Schema(description = "부서 코드. 생략하면 서버에서 자동 생성합니다.", example = "DEPT_001")
    private String deptCd;

    @Schema(description = "상위 부서 코드. 최상위 부서이면 생략합니다.", example = "DEPT_001")
    private String parentDeptCd;

    @NotBlank
    @Schema(description = "부서명", example = "개발팀")
    private String deptNm;
}
