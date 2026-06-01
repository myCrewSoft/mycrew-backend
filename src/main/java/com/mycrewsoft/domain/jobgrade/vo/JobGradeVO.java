package com.mycrewsoft.domain.jobgrade.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.mycrewsoft.domain.employee.vo.EmployeeVO;

import lombok.Getter;
import lombok.Setter;

/**
 * 직급 정보를 담는 VO.
 * 대응 테이블: TB_JOB_GRADE
 */
@Getter
@Setter
public class JobGradeVO {

    private String jobGrdCd;
    private String jobGrdNm;
    private String useYn;
    private Long frstRgtrId;
    private LocalDateTime frstRegDt;
    private Long lastMdfrId;
    private LocalDateTime lastMdfcnDt;
    // Has
    private List<EmployeeVO> employeeList;
}
