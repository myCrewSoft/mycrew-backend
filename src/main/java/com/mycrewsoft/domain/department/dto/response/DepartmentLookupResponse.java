package com.mycrewsoft.domain.department.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentLookupResponse {

    @Schema(description = "부서 코드", example = "DEPT01")
    private String deptCd;

    @Schema(description = "부서명", example = "개발팀")
    private String deptNm;
}