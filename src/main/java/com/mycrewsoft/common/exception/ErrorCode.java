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

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER_003", "이미 사용 중인 닉네임입니다."),
    USER_DISABLED(HttpStatus.FORBIDDEN, "USER_004", "사용자 계정이 비활성화되었습니다."),
    DUPLICATE_EMPLOYEEID(HttpStatus.CONFLICT, "USER_005", "이미 사용 중인 사원번호입니다."),

    // FILE
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_001", "파일 업로드에 실패했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE_002", "허용되지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_003", "파일 크기가 초과되었습니다.");

    // 팀원이 새 도메인 추가 시 아래 패턴으로 섹션 추가
    // BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_001", "존재하지 않는 게시글입니다."),

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
