package com.mycrewsoft.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 프로젝트 전체 에러 코드를 관리하는 enum.
 * HTTP 상태코드, 에러 식별 코드, 사람이 읽을 메시지를 한 곳에서 정의한다.
 *
 * HTTP 상태코드 가이드:
 * 400 BAD_REQUEST → 클라이언트가 잘못된 요청을 보낸 경우
 * 401 UNAUTHORIZED → 인증이 안 된 경우 (토큰 없음, 만료, 변조, 권한 버전 불일치)
 * 403 FORBIDDEN → 인증은 됐지만 권한이 없는 경우
 * 404 NOT_FOUND → 요청한 리소스가 없는 경우
 * 409 CONFLICT → 이미 존재하는 리소스와 충돌
 * 500 INTERNAL_SERVER_ERROR → 서버 내부 오류
 */
@Getter
public enum ErrorCode {
	
	// APPROVAL
	TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "APR_001", "존재하지 않는 결재 양식입니다."),
	APPROVAL_DOC_NOT_FOUND(HttpStatus.NOT_FOUND, "APR_002", "존재하지 않는 기안서입니다."),
	APPROVAL_ALREADY_PROCESSED(HttpStatus.CONFLICT, "APR_003", "이미 처리된 결재가 있어 회수할 수 없습니다."),
	APPROVAL_DOC_STATUS_INVALID(HttpStatus.BAD_REQUEST, "APR_004", "현재 상태에서 처리할 수 없는 결재 문서입니다."),
	APPROVAL_LINE_INVALID(HttpStatus.BAD_REQUEST, "APR_005", "결재선 정보가 올바르지 않습니다."),
	APPROVAL_REJECT_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "APR_006", "반려 사유는 필수입니다."),
	APPROVAL_SIGNATURE_REQUIRED(HttpStatus.BAD_REQUEST, "APR_007", "전자서명 이미지가 등록되어 있지 않아 승인할 수 없습니다. 마이페이지에서 전자서명을 먼저 등록해 주세요."),

    // COMMON
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_003", "허용되지 않는 HTTP 메서드입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "COMMON_004", "요청 값의 타입이 올바르지 않습니다."),

    // AUTH
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_002", "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "만료된 토큰입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_005", "아이디 또는 비밀번호가 올바르지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_006", "Refresh Token 이 존재하지 않습니다."),
    AUTH_VERSION_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_007", "Token authorization version이 일치하지 않습니다."),
    OAUTH_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_008", "OAuth 처리 중 오류가 발생했습니다."),
    PASSWORD_NOT_MATCHED(HttpStatus.UNAUTHORIZED, "AUTH_009", "비밀번호가 일치하지 않습니다."),
    
    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER_003", "이미 사용 중인 닉네임입니다."),
    USER_DISABLED(HttpStatus.FORBIDDEN, "USER_004", "사용자 계정이 비활성화되었습니다."),
    DUPLICATE_EMPLOYEEID(HttpStatus.CONFLICT, "USER_005", "이미 사용 중인 사원번호입니다."),

    // NOTIFICATION
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIF_001", "존재하지 않는 알림입니다."),

    // FILE
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_001", "파일 업로드에 실패했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE_002", "허용되지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_003", "파일 크기가 초과되었습니다."),
	FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_004", "존재하지 않는 파일입니다."),
	
	// SCHEDULE
	NOT_SCHEDULE_OWNER(HttpStatus.FORBIDDEN, "SCH-001", "해당 일정에 대한 관리 권한이 없습니다."),
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCH-002", "요청하신 일정을 찾을 수 없습니다."),
	
	// DRIVE
	DRIVE_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DRIVE_001", "폴더 생성에 실패했습니다."),
    DRIVE_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "DRIVE_002", "드라이브 아이템을 찾을 수 없습니다."),
    DRIVE_RENAME_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "DRIVE_003", "폴더만 이름 수정이 가능합니다."),
    DRIVE_NOT_A_FILE(HttpStatus.BAD_REQUEST, "DRIVE_004", "파일 아이템이 아닙니다."),

    // BOARD
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_001", "존재하지않는 게시판입니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_002", "존재하지않는 댓글입니다."),
    // JOB
    RANK_NOT_FOUND(HttpStatus.NOT_FOUND, "JOB_001", "존재하지 않는 직급입니다."),
    DUPLICATE_RANK_ID(HttpStatus.CONFLICT, "JOB_002", "이미 존재하는 직급 ID입니다."),

    // ROLE 
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROLE_001", "존재하지 않는 역할입니다."),
    DUPLICATE_ROLE_CODE(HttpStatus.CONFLICT, "ROLE_002", "해당 역할 코드가 이미 존재합니다."),
	
	// PERMISSION
	PERMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "PERM_001", "존재하지 않는 권합입니다."),

    // MAIL
    MAIL_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "MAIL_001", "연동된 활성 메일 계정을 찾을 수 없습니다."),
    MAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "MAIL_002", "존재하지 않는 메일입니다."),
    MAIL_SCOPE_REQUIRED(HttpStatus.FORBIDDEN, "MAIL_003", "메일 API 사용에 필요한 Google OAuth scope가 부족합니다."),
    MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MAIL_004", "메일 발송 중 오류가 발생했습니다."),
    MAIL_SYNC_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MAIL_005", "메일 동기화 중 오류가 발생했습니다."),

    // MESSENGER
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND,  "CHAT_001", "존재하지 않는 채팅방입니다."),
    CHAT_NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "CHAT_002", "채팅방 참여자가 아닙니다."),
    CHAT_ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "CHAT_003", "이미 존재하는 1:1 채팅방입니다."),
    CHAT_DIRECT_ROOM_CANNOT_ADD_PARTICIPANT(HttpStatus.BAD_REQUEST, "CHAT_004", "1:1 채팅방에는 참여자를 추가할 수 없습니다."),
    CHAT_OWNER_CANNOT_BE_REMOVED(HttpStatus.BAD_REQUEST, "CHAT_005", "채팅방 개설자는 제거할 수 없습니다."),

    // WEBSOCKET
    WS_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "WS_001", "WebSocket 연결 시 인증이 필요합니다."),
    WS_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "WS_002", "WebSocket 연결 시 유효하지 않은 토큰입니다."),

    // DEPARTMENT
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DEPT_001", "존재하지 않는 부서입니다."),
    DUPLICATE_DEPARTMENT_CODE(HttpStatus.CONFLICT, "DEPT_002", "이미 존재하는 부서 코드입니다."),

	// ROOM
	CONF_RM_NOT_FOUND(HttpStatus.NOT_FOUND, "CONF_RM_001", "존재하지 않는 회의실입니다."),
	CONF_RM_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONF_RM_002", "회의실 생성에 실패했습니다."),
	CONF_RM_ALREADY_DISABLED(HttpStatus.BAD_REQUEST, "CONF_RM_003", "이미 비활성화된 회의실입니다."),

	// RESERVATION 
	RSRV_NOT_FOUND(HttpStatus.NOT_FOUND, "RSRV_001", "존재하지 않는 예약입니다."),
	RSRV_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RSRV_002", "예약 생성에 실패했습니다."),
	RSRV_TIME_CONFLICT(HttpStatus.CONFLICT, "RSRV_003", "이미 예약된 시간대입니다."),
	
	// PROEJCT
	PROJECT_INVALID_DATE(HttpStatus.BAD_REQUEST, "PROJECT_001", "종료날짜는 시작날짜 이후여야 합니다."),
	PROJECT_NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "PROJECT_002", "프로젝트 참여자가 아닙니다."),
	PROJECT_NOT_OWNER(HttpStatus.FORBIDDEN, "PROJECT_003", "해당 업무에 대한 권한이 없습니다."),
	PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_004", "존재하지 않는 프로젝트입니다."),
	PROJECT_INVALID_STAT_TRANSITION(HttpStatus.BAD_REQUEST, "PROJECT_005", "해당 상태에서는 상태를 변경할 수 없습니다."),
	PROJECT_CANNOT_MODIFY_DATE(HttpStatus.BAD_REQUEST, "PROJECT_006", "해당 상태에서는 날짜를 수정할 수 없습니다."),
	PROJECT_LEADER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "PROJECT_007", "프로젝트 장은 퇴출시킬 수 없습니다."),
  
    // VIDEO
    VIDEO_CONF_NOT_FOUND(HttpStatus.NOT_FOUND,   "VIDEO_001", "존재하지 않는 화상회의입니다."),
    VIDEO_MOM_NOT_FOUND(HttpStatus.NOT_FOUND,    "VIDEO_002", "존재하지 않는 회의록입니다."),
    VIDEO_ACCESS_DENIED(HttpStatus.FORBIDDEN,    "VIDEO_003", "화상회의 참여자만 접근할 수 있습니다."),
    VIDEO_ALREADY_ENDED(HttpStatus.BAD_REQUEST,  "VIDEO_004", "이미 종료된 화상회의입니다."),
    VIDEO_MOM_ALREADY_CONFIRMED(HttpStatus.BAD_REQUEST, "VIDEO_005", "이미 확정된 회의록입니다."),
    VIDEO_APRVL_ALREADY_DONE(HttpStatus.BAD_REQUEST,    "VIDEO_006", "이미 결재 처리된 항목입니다."),
    STT_TRANSCRIBE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "VIDEO_007", "음성 텍스트 변환에 실패했습니다."),
	
	// TASK
	TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "TASK_001", "존재하지 않는 업무입니다."),
	TASK_NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "TASK_002", "업무 참여자가 아닙니다."),
	TASK_NOT_OWNER(HttpStatus.FORBIDDEN, "TASK_003", "해당 업무에 대한 권한이 없습니다.");
    // 팀원이 새 도메인 추가 시 아래 패턴으로 섹션 추가
    // BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_001", "존재하지 않는 게시글입니다.")
	
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
