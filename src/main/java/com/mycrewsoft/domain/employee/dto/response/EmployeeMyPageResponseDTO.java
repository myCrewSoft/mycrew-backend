package com.mycrewsoft.domain.employee.dto.response;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.mycrewsoft.domain.department.vo.DepartmentVO;
import com.mycrewsoft.domain.jobgrade.vo.JobGradeVO;
import com.mycrewsoft.domain.jobposition.vo.JobPositionVO;
import com.mycrewsoft.domain.roleassignment.vo.RoleAssignmentVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "직원 마이페이지 응답 DTO")
@Setter
@Getter
public class EmployeeMyPageResponseDTO {
	@Schema(description = "직원 ID")
	private Long empId;
	
	@Schema(description = "직원명")
    private String empNm;
	
	@Schema(description = "이메일 주소")
	private String emailAddr;
	
	@Schema(description = "프로필 이미지 파일 ID")
    private Long prflImgFileId;
	
	@Schema(description = "성별 코드")
    private String genderCd;
	
	@Schema(description = "휴대전화번호")
	private String mblTelno;
	
	@Schema(description = "우편번호")
    private String zip;

    @Schema(description = "주소")
    private String addr;
	
    @Schema(description = "임원 여부")
    private String execYn;
    
    @Schema(description = "전자서명 파일 ID")
    private Long mbrStampFileId;
    
    @Schema(description = "직무 내용")
    private String jobDutyCn;
    
    @Schema(description = "역할 할당 목록")
    private List<RoleAssignmentVO> roleAssignmentList;
    
    @Schema(description = "입사일")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate entcoYmd;
    
    @Schema(description = "부서")
    private DepartmentVO department;
    
	@Schema(description = "직위")
	private JobPositionVO jobPosition;
    
	@Schema(description = "직급")
	private JobGradeVO jobGrade;
}
