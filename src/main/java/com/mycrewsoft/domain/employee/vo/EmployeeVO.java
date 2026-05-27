package com.mycrewsoft.domain.employee.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.mycrewsoft.domain.authversion.vo.AuthVersionVO;
import com.mycrewsoft.domain.department.vo.DepartmentVO;
import com.mycrewsoft.domain.empstat.vo.EmpStatVO;
import com.mycrewsoft.domain.jobgrade.vo.JobGradeVO;
import com.mycrewsoft.domain.jobposition.vo.JobPositionVO;
import com.mycrewsoft.domain.messenger.vo.MsngrChtrmPtcptVO;
import com.mycrewsoft.domain.roleassignment.vo.RoleAssignmentVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 사원의 정보를 담는 VO 클래스
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"pswd", "rrno"})
public class EmployeeVO {

    /** 회원ID, PK */
    private Long empId;

    /** 부서코드 */
    private String deptCd;

    /** 직책코드 */
    private String jobPstnCd;

    /** 직급코드 */
    private String jobGrdCd;

    /** 사원상태코드 */
    private String empStatCd;

    /** 직무내용 */
    private String jobDutyCn;

    /** 비밀번호 */
    private String pswd;

    /** 사원명 */
    private String empNm;

    /** 주민등록번호 */
    private String rrno;

    /** 성별코드 */
    private String genderCd;

    /** 휴대전화번호 */
    private String mblTelno;

    /** 우편번호 */
    private String zip;

    /** 주소 */
    private String addr;

    /** 프로필이미지파일ID */
    private Long prflImgFileId;

    /** 임원여부 */
    private String execYn;

    /** 입사일자 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate entcoYmd;

    /** 퇴사일자 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate retcoYmd;

    /** 최초등록일시 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime frstRegDt;

    /** 최종수정일시 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMdfcnDt;

    /** 계정 사용여부 */
    @Builder.Default
    private String enabled = "n";

    /** 사원서명파일ID */
    private Long mbrStampFileId;
    
    // Is / belongs-to
    /** 사원의 부서 */
    private DepartmentVO department;
    /** 사원의 직책 */
    private JobPositionVO jobPosition;
    /** 사원의 직급 */
    private JobGradeVO jobGrade;
    /** 사원의 상태 */
    private EmpStatVO empStat;

    // Has
    /** 사원의 권한버전 */
    private AuthVersionVO authVersion;
    /** 사원의 역할매핑 */
    private List<RoleAssignmentVO> roleAssignmentList;
//    private List<DriveVO> driveList;
//    private List<MailAccountVO> mailAccountList;
//    private List<AprvlDocVO> draftedAprvlDocList;
//    private List<ProjMemberVO> projMemberList;
//    private List<TaskMemberVO> taskMemberList;
    /** 사원의 참여채팅방 */
    private List<MsngrChtrmPtcptVO> msngrChtrmPtcptList;
}
