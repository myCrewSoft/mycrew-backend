package com.mycrewsoft.domain.employee.dto.response;

import com.mycrewsoft.domain.department.vo.DepartmentVO;
import com.mycrewsoft.domain.jobgrade.vo.JobGradeVO;
import com.mycrewsoft.domain.jobposition.vo.JobPositionVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사원 프로필 응답 DTO")
public class EmployeeProfileDTO {
	@Schema(description = "사원명")
    private String empNm;
	
	@Schema(description = "프로필ID")
    private Long prflImgFileId;
	
	@Schema(description = "부서")
    private DepartmentVO department;
    
	@Schema(description = "직책")
	private JobPositionVO jobPosition;
    
	@Schema(description = "직급")
	private JobGradeVO jobGrade;
}
