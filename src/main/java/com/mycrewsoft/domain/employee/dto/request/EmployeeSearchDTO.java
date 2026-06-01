package com.mycrewsoft.domain.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 관리자가 사원 목록을 조회하거나 검색할 때 사용하는 DTO 클래스. 
 * 검색어(keyword), 부서코드(deptCd), 사원상태(empStatCd), 
 * 직급코드(jobGrdCd), 직책코드(jobPstnCd), 임원여부(execYn), 
 * 사용여부(enabled) 등의 필드를 포함한다. 또한, 페이지네이션을 위한 page와 size 필드도 포함한다.
 */
@Getter
@Setter
@Schema(description = "사원 목록, 조회 요청 DTO")
public class EmployeeSearchDTO {
	
	@Schema(description = "검색어", example = "홍길동")
    private String keyword;
	
	@Schema(description = "부서코드", example = "DEPT_01")
    private String deptCd;
	
	@Schema(description = "직원상태", example = "EMP_ACTIVITY")
    private String empStatCd;
	
	@Schema(description = "직급코드", example = "JOB_GRD_01")
    private String jobGrdCd;
	
	@Schema(description = "직책코드", example = "JOB_PSTN_01")
    private String jobPstnCd;
	
	@Schema(description = "임원", example = "Y")
    private String execYn;
    
	@Schema(description = "사용여부", example = "Y")
    private String enabled;
    
    private int page = 0;
    private int size = 10;
}
