package com.mycrewsoft.security.authz;

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
	
	//ADMIN
	ADMIN_CONSOLE_ACCESS,
	ROLE_MANAGE,
	EMPLOYEE_CREATE,
	EMPLOYEE_READ,
	EMPLOYEE_UPDATE,
	
    // Department
    DEPT_CREATE,
    DEPT_READ,
    DEPT_UPDATE,
    DEPT_DELETE,
    DEPT_MEMBER_MANAGE,

    // Project
    PROJECT_CREATE,
    PROJECT_READ,
    PROJECT_UPDATE,
    PROJECT_DELETE,
    PROJECT_MEMBER_MANAGE,

    // Mail
    MAIL_READ,
    MAIL_SEND,
    MAIL_DELETE,
    MAIL_MANAGE,

    // Drive
    DRIVE_READ,
    DRIVE_UPLOAD,
    DRIVE_UPDATE,
    DRIVE_DELETE,
    DRIVE_MANAGE,

    // Project drive
    PROJECT_DRIVE_READ,
    PROJECT_DRIVE_UPLOAD,
    PROJECT_DRIVE_UPDATE,
    PROJECT_DRIVE_DELETE,
    PROJECT_DRIVE_MANAGE,

    // Board
    BOARD_POST_CREATE,
    BOARD_POST_READ,
    BOARD_POST_UPDATE,
    BOARD_POST_DELETE,
    BOARD_POST_MANAGE,

    // Education
    EDU_COURSE_CREATE,
    EDU_COURSE_READ,
    EDU_COURSE_UPDATE,
    EDU_COURSE_DELETE,
    EDU_ENROLL_MANAGE,

    // Attendance
    ATTENDANCE_READ,
    ATTENDANCE_UPDATE,
    ATTENDANCE_DEPT_READ,
    ATTENDANCE_APPROVE,
    ATTENDANCE_MANAGE,

	// Schedule
	SCHEDULE_CREATE,
	SCHEDULE_READ,
	SCHEDULE_UPDATE,
	SCHEDULE_DELETE,
	SCHEDULE_MANAGE;
	
    public String getCode() {
        return name();
    }
}
