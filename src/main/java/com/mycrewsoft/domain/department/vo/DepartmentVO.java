package com.mycrewsoft.domain.department.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.mycrewsoft.domain.employee.vo.EmployeeVO;

import lombok.Getter;
import lombok.Setter;

/**
 * 부서 정보를 담는 VO.
 * 대응 테이블: TB_DEPARTMENT
 */
@Getter
@Setter
public class DepartmentVO {

    private String deptCd;
    private String prntDeptCd;
    private String deptNm;
    private String useYn;
    private LocalDateTime frstRegDt;
    private Long frstRgtrId;
    private LocalDateTime lastMdfcnDt;
    private Long lastMdfrId;
    
    // Is / belongs-to
    private DepartmentVO parentDepartment;

    // Has
    private List<DepartmentVO> childDepartmentList;
    private List<EmployeeVO> employeeList;
}
