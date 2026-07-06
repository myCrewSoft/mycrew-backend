package com.mycrewsoft.domain.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLookupRequest {

    @Schema(description = "통합 검색어 (사원명·부서명·직급명 중 하나)", example = "김철수")
    private String keyword;

    @Schema(description = "부서 코드 필터 (선택)", example = "DEPT01")
    private String deptCd;
    
    @Schema(description = "특정 프로젝트 참가자만 조회 (프로젝트 ID)", example = "1")
    private Long projId;
    
    @Schema(description = "특정 프로젝트 참가자 제외 (프로젝트 ID)")
    private Long excludeProjId;
    
    @Schema(description = "검색 결과에서 제외할 사원 ID (본인 제외용)", example = "1001")
    private Long excludeEmpId;

    @Schema(description = "특정 부서 사원만 조회 (부서 코드)", example = "DEPT01")
    private String fixedDeptCd;

}