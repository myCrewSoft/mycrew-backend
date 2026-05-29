package com.mycrewsoft.domain.employee.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.mycrewsoft.common.util.DateUtil;
import com.mycrewsoft.domain.department.vo.DepartmentVO;
import com.mycrewsoft.domain.empstat.vo.EmpStatVO;
import com.mycrewsoft.domain.jobgrade.vo.JobGradeVO;
import com.mycrewsoft.domain.jobposition.vo.JobPositionVO;
import com.mycrewsoft.domain.roleassignment.vo.RoleAssignmentVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사원의 목록 조회 응답을 위한 DTO 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "사원 목록 조회 응답 DTO")
public class EmployeeListDTO {
	
    private Long empId;
    private String deptCd;
    private String jobPstnCd;
    private String jobGrdCd;
    private String empStatCd;
    private String jobDutyCn;
    private String empNm;
    private String genderCd;
    private String mblTelno;
    private String zip;
    private String addr;
    private Long prflImgFileId;
    private String execYn;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate entcoYmd;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate retcoYmd;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime frstRegDt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMdfcnDt;

    private String enabled;

    @Schema(description = "부서")
    private DepartmentVO department;

    @Schema(description = "직책")
    private JobPositionVO jobPosition;

    @Schema(description = "직급")
    private JobGradeVO jobGrade;

    @Schema(description = "직원상태")
    private EmpStatVO empStat;

    private List<RoleAssignmentVO> roleAssignmentList;
}
