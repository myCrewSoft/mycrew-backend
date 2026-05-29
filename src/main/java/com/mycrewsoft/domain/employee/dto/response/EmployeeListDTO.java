package com.mycrewsoft.domain.employee.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

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
 * 관리자 사원 목록 조회 응답 DTO.
 *
 * 프론트에서 코드값만 다시 해석하지 않도록 부서, 직책, 직급, 직원상태 VO를 함께 내려준다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "사원 목록 조회 응답 DTO")
public class EmployeeListDTO {

    @Schema(description = "사원 ID")
    private Long empId;

    @Schema(description = "부서 코드")
    private String deptCd;

    @Schema(description = "직책 코드")
    private String jobPstnCd;

    @Schema(description = "직급 코드")
    private String jobGrdCd;

    @Schema(description = "직원 상태 코드")
    private String empStatCd;

    @Schema(description = "직무 내용")
    private String jobDutyCn;

    @Schema(description = "사원명")
    private String empNm;

    @Schema(description = "성별 코드")
    private String genderCd;

    @Schema(description = "휴대폰 번호")
    private String mblTelno;

    @Schema(description = "우편번호")
    private String zip;

    @Schema(description = "주소")
    private String addr;

    @Schema(description = "프로필 이미지 파일 ID")
    private Long prflImgFileId;

    @Schema(description = "임원 여부")
    private String execYn;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "입사일")
    private LocalDate entcoYmd;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "퇴사일")
    private LocalDate retcoYmd;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "최초 등록 일시")
    private LocalDateTime frstRegDt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "최종 수정 일시")
    private LocalDateTime lastMdfcnDt;

    @Schema(description = "사용 여부")
    private String enabled;

    @Schema(description = "부서")
    private DepartmentVO department;

    @Schema(description = "직책")
    private JobPositionVO jobPosition;

    @Schema(description = "직급")
    private JobGradeVO jobGrade;

    @Schema(description = "직원상태")
    private EmpStatVO empStat;

    @Schema(description = "역할 부여 목록")
    private List<RoleAssignmentVO> roleAssignmentList;
}
