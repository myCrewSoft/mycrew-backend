package com.mycrewsoft.domain.jobposition.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.mycrewsoft.domain.employee.vo.EmployeeVO;

import lombok.Getter;
import lombok.Setter;

/**
 * 직위 정보를 담는 VO.
 * 대응 테이블: TB_JOB_POSITION
 */
@Getter
@Setter
public class JobPositionVO {

    private String jobPstnCd;
    private String jobPstnNm;
    private String useYn;
    private Long frstRgtrId;
    private LocalDateTime frstRegDt;
    private Long lastMdfrId;
    private LocalDateTime lastMdfcnDt;
    
    // Has
    private List<EmployeeVO> employeeList;
}
