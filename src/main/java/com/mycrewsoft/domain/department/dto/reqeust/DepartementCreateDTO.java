package com.mycrewsoft.domain.department.dto.reqeust;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class DepartementCreateDTO {
	@Schema(description = "상위 부서 코드", example = "DEPT_001")
	private String prntDeptCd;
	
	@NotBlank(message = "부서 이름은 필수입니다.")
	@Schema(description = "부서 이름", example = "인사팀")
	private String deptNm;
}
