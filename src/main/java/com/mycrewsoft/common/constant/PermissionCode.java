package com.mycrewsoft.common.constant;

/**
 * 시스템에서 사용하는 권한 코드를 한 곳에서 관리하는 enum.
 *
 * 역할:
 * - 도메인 서비스 코드에서 권한 문자열을 직접 입력하지 않도록 한다.
 * - enum 이름 자체를 TB_PERMISSION.PERM_CD, Redis scopedPermissions.permCd와 동일하게 사용한다.
 * - 권한 코드 문자열은 getCode() 또는 name()으로 얻는다.
 *
 * 사용 예:
 * - authorizationService.assertCurrentUserPermission(PermissionCode.MAIL_READ, resource)
 * - PermissionCode.MAIL_READ.getCode() -> "MAIL_READ"
 */
public enum PermissionCode {	
	// ADMIN
	ADMIN_CONSOLE_ACCESS("관리자 페이지 접속 권한", "관리자 페이지에 접속할 수 있는 권한입니다."),
	ADMIN_ROLE_MANAGE("역할 관리", "역할을 생성, 조회, 수정 및 관리할 수 있는 권한입니다."),
	ADMIN_JOB_MANAGE("직급 관리", "직급을 생성, 조회, 수정 및 삭제할 수 있는 권한입니다."),
	ADMIN_EMPLOYEE_CREATE("사원 등록", "사원 정보를 등록할 수 있는 권한입니다."),
	ADMIN_EMPLOYEE_READ("사원 조회", "사원 정보를 조회할 수 있는 권한입니다."),
	ADMIN_EMPLOYEE_UPDATE("사원 수정", "사원 정보를 수정할 수 있는 권한입니다."),
	ADMIN_DEPT_CREATE("ADMIN 부서 생성", "부서를 생성할 수 있는 권한입니다."),
	ADMIN_DEPT_READ("ADMIN 부서 조회", "부서 정보를 조회할 수 있는 권한입니다."),
	ADMIN_DEPT_UPDATE("ADMIN 부서 수정", "부서 정보를 수정할 수 있는 권한입니다."),
	ADMIN_DEPT_DELETE("ADMIN 부서 삭제", "부서를 삭제할 수 있는 권한입니다."),
	ADMIN_DEPT_MEMBER_MANAGE("ADMIN 부서 구성원 관리", "부서 구성원을 배정, 이동 및 관리할 수 있는 권한입니다."),
	
	//APPROVAL
	APPROVAL_DRAFT_CREATE("결재 기안 생성", "결재 기안을 생성할 수 있는 권한입니다."),
	APPROVAL_DRAFT_UPDATE("결재 기안 수정", "결재 기안을 수정할 수 있는 권한입니다."),
	APPROVAL_DRAFT_DELETE("결재 기안 삭제", "결재 기안을 삭제할 수 있는 권한입니다."),
	APPROVAL_TEMPLATE_CREATE("결재 양식 생성", "결재 양식을 생성할 수 있는 권한입니다."),
	APPROVAL_TEMPLATE_UPDATE("결재 양식 수정", "결재 양식을 수정할 수 있는 권한입니다."),
	APPROVAL_TEMPLATE_DELETE("결재 양식 수정", "결재 양식을 삭제할 수 있는 권한입니다."),
	
	// Department
	DEPT_CREATE("부서 생성", "부서를 생성할 수 있는 권한입니다."),
	DEPT_READ("부서 조회", "부서 정보를 조회할 수 있는 권한입니다."),
	DEPT_UPDATE("부서 수정", "부서 정보를 수정할 수 있는 권한입니다."),
	DEPT_DELETE("부서 삭제", "부서를 삭제할 수 있는 권한입니다."),
	DEPT_MEMBER_MANAGE("부서 구성원 관리", "부서 구성원을 추가, 변경 및 관리할 수 있는 권한입니다."),

	// Project
	PROJECT_CREATE("프로젝트 생성", "프로젝트를 생성할 수 있는 권한입니다."),
	PROJECT_READ("프로젝트 조회", "프로젝트 정보를 조회할 수 있는 권한입니다."),
	PROJECT_UPDATE("프로젝트 수정", "프로젝트 정보를 수정할 수 있는 권한입니다."),
	PROJECT_DELETE("프로젝트 삭제", "프로젝트를 삭제할 수 있는 권한입니다."),
	PROJECT_MEMBER_MANAGE("프로젝트 멤버 관리", "프로젝트 참여자를 추가, 변경 및 관리할 수 있는 권한입니다."),

	// Mail
	MAIL_READ("메일 조회", "메일을 조회할 수 있는 권한입니다."),
	MAIL_SEND("메일 발송", "메일을 발송할 수 있는 권한입니다."),
	MAIL_DELETE("메일 삭제", "메일을 삭제할 수 있는 권한입니다."),
	MAIL_MANAGE("메일 관리", "메일 관련 기능을 관리할 수 있는 권한입니다."),

	// Drive
	DRIVE_READ("드라이브 조회", "드라이브 파일 및 폴더를 조회할 수 있는 권한입니다."),
	DRIVE_UPLOAD("드라이브 업로드", "드라이브에 파일을 업로드할 수 있는 권한입니다."),
	DRIVE_UPDATE("드라이브 수정", "드라이브 파일 및 폴더를 수정할 수 있는 권한입니다."),
	DRIVE_DELETE("드라이브 삭제", "드라이브 파일 및 폴더를 삭제할 수 있는 권한입니다."),
	DRIVE_MANAGE("드라이브 관리", "드라이브 전체 기능을 관리할 수 있는 권한입니다."),

	// Project Drive
	PROJECT_DRIVE_READ("프로젝트 드라이브 조회", "프로젝트 드라이브의 파일 및 폴더를 조회할 수 있는 권한입니다."),
	PROJECT_DRIVE_UPLOAD("프로젝트 드라이브 업로드", "프로젝트 드라이브에 파일을 업로드할 수 있는 권한입니다."),
	PROJECT_DRIVE_UPDATE("프로젝트 드라이브 수정", "프로젝트 드라이브의 파일 및 폴더를 수정할 수 있는 권한입니다."),
	PROJECT_DRIVE_DELETE("프로젝트 드라이브 삭제", "프로젝트 드라이브의 파일 및 폴더를 삭제할 수 있는 권한입니다."),
	PROJECT_DRIVE_MANAGE("프로젝트 드라이브 관리", "프로젝트 드라이브 전체 기능을 관리할 수 있는 권한입니다."),

	// Board
	BOARD_POST_CREATE("게시글 작성", "게시글을 작성할 수 있는 권한입니다."),
	BOARD_POST_READ("게시글 조회", "게시글을 조회할 수 있는 권한입니다."),
	BOARD_POST_UPDATE("게시글 수정", "게시글을 수정할 수 있는 권한입니다."),
	BOARD_POST_DELETE("게시글 삭제", "게시글을 삭제할 수 있는 권한입니다."),
	BOARD_POST_MANAGE("게시판 관리", "게시판 및 게시글을 관리할 수 있는 권한입니다."),

	// Education
	EDU_COURSE_CREATE("교육 과정 생성", "교육 과정을 생성할 수 있는 권한입니다."),
	EDU_COURSE_READ("교육 과정 조회", "교육 과정을 조회할 수 있는 권한입니다."),
	EDU_COURSE_UPDATE("교육 과정 수정", "교육 과정을 수정할 수 있는 권한입니다."),
	EDU_COURSE_DELETE("교육 과정 삭제", "교육 과정을 삭제할 수 있는 권한입니다."),
	EDU_ENROLL_MANAGE("수강 관리", "교육 과정의 수강 신청 및 수강자를 관리할 수 있는 권한입니다."),

	// Attendance
	ATTENDANCE_READ("근태 조회", "근태 정보를 조회할 수 있는 권한입니다."),
	ATTENDANCE_UPDATE("근태 수정", "근태 정보를 수정할 수 있는 권한입니다."),
	ATTENDANCE_DEPT_READ("부서 근태 조회", "부서 구성원의 근태 정보를 조회할 수 있는 권한입니다."),
	ATTENDANCE_APPROVE("근태 승인", "근태 관련 요청을 승인 또는 반려할 수 있는 권한입니다."),
	ATTENDANCE_MANAGE("근태 관리", "근태 기능 전반을 관리할 수 있는 권한입니다."),

	// Schedule
	SCHEDULE_CREATE("일정 생성", "일정을 생성할 수 있는 권한입니다."),
	SCHEDULE_READ("일정 조회", "일정을 조회할 수 있는 권한입니다."),
	SCHEDULE_UPDATE("일정 수정", "일정을 수정할 수 있는 권한입니다."),
	SCHEDULE_DELETE("일정 삭제", "일정을 삭제할 수 있는 권한입니다."),
	SCHEDULE_MANAGE("일정 관리", "일정 기능 전반을 관리할 수 있는 권한입니다.");
	
	
	private final String PermissionName;
	private final String description;
	
	PermissionCode(String PermissionName, String description) {
		this.PermissionName = PermissionName;
        this.description = description;
	}
	
	public String getPermissionName() {
		return PermissionName;
	}

	public String getDescription() {
		return description;
	}
	
	public String getCode() {
        return name();
    }
}
