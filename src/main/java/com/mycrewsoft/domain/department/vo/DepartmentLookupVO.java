package com.mycrewsoft.domain.department.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentLookupVO {

    private String deptCd;  // 부서 코드
    private String deptNm;  // 부서 이름
}