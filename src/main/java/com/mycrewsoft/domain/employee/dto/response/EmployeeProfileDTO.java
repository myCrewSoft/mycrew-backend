package com.mycrewsoft.domain.employee.dto.response;

import com.mycrewsoft.domain.department.vo.DepartmentVO;
import com.mycrewsoft.domain.jobgrade.vo.JobGradeVO;
import com.mycrewsoft.domain.jobposition.vo.JobPositionVO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeProfileDTO {
    private String empNm;
    private Long prflImgFileId;
    private DepartmentVO department;
    private JobPositionVO jobPosition;
    private JobGradeVO jobGrade;
}
