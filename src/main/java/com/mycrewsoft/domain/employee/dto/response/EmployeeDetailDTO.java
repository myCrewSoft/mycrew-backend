package com.mycrewsoft.domain.employee.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.mycrewsoft.domain.department.vo.DepartmentVO;
import com.mycrewsoft.domain.empstat.vo.EmpStatVO;
import com.mycrewsoft.domain.jobgrade.vo.JobGradeVO;
import com.mycrewsoft.domain.jobposition.vo.JobPositionVO;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;
import com.mycrewsoft.domain.roleassignment.vo.RoleAssignmentVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자의 사원 상세 조회 응답을 위한 DTO 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "사원 상세 조회 응답 DTO")
public class EmployeeDetailDTO {
	
	@Schema(description = "사번", example = "20230122")
    private Long empId;
	
	@Schema(description = "직무설명", example = "개발부 직원으로써 개발에 힘...")
    private String jobDutyCn;
	
	@Schema(description = "사원명", example = "홍길동")
    private String empNm;
	
	@Schema(description = "성별", example = "Y")
    private String genderCd;
	
	@Schema(description = "전화번호", example = "010-1234-5678")
    private String mblTelno;
	
	@Schema(description = "우편번호", example = "12345")
    private String zip;
	
	@Schema(description = "주소", example = "서울시")
    private String addr;
	
	@Schema(description = "프로필사진 ID")
    private Long prflImgFileId;
	
	@Schema(description = "간부여부", example = "Y")
    private String execYn;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "입사일")
    private LocalDate entcoYmd;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "퇴사일")
    private LocalDate retcoYmd;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "최초등록일시")
    private LocalDateTime frstRegDt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "최종수정일시")
    private LocalDateTime lastMdfcnDt;

    @Schema(description = "사용여부", example = "Y")
    private String enabled;
    
    @Schema(description = "사원도장 ID")
    private Long mbrStampFileId;

    @Schema(description = "부서")
    private DepartmentVO department;
    
    @Schema(description = "직책")
    private JobPositionVO jobPosition;
    
    @Schema(description = "직급")
    private JobGradeVO jobGrade;
    
    @Schema(description = "직원상태")
    private EmpStatVO empStat;

    @Schema(description = "역할")
    private List<RoleAssignmentVO> roleAssignmentList;
    
    @Schema(description = "메일")
    private List<MailAccountVO> mailAccountList;
}
