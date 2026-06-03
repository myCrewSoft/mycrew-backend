package com.mycrewsoft.domain.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLookupRequest {

    @Schema(description = "통합 검색어 (사원명·부서명·직급명 중 하나)", example = "김철수")
    private String keyword;

    @Schema(description = "부서 코드 필터 (선택)", example = "DEPT01")
    private String deptCd;
}