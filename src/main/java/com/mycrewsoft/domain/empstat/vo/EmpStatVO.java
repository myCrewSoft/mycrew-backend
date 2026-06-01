package com.mycrewsoft.domain.empstat.vo;

import java.util.List;

import com.mycrewsoft.domain.employee.vo.EmployeeVO;

import lombok.Getter;
import lombok.Setter;

/**
 * 사원 상태 코드 정보를 담는 VO.
 * 대응 테이블: TB_EMP_STAT
 */
@Getter
@Setter
public class EmpStatVO {

    private String empStatCd;
    private String empStatNm;
    private String empStatExpln;
    
    // Has
    private List<EmployeeVO> employeeList;
}
