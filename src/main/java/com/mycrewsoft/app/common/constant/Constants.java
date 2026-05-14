package com.mycrewsoft.app.common.constant;

/**
 * 프로젝트 전역에서 공통으로 사용하는 상수를 관리하는 클래스.
 * 
 * 문자열 하드코딩을 줄이고, 중복되는 값을 한 곳에서 관리하기 위해 사용한다.
 * 상수를 중앙에서 관리함으로써 유지보수성과 가독성을 높이고,
 * 오타 및 값 불일치 문제를 방지하는 것을 목적으로 한다.
 */
 public final class Constants {

    private Constants() {}
		// Auth
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
		// Member
    public static final String Member = "member";

    // Security
    /** 인증 없이 접근 허용할 URL. 공개 API 추가 시 AA 에게 요청한다. */
    public static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**", // 로그인, 회원가입, 토큰 재발급
            "/swagger-ui/**", // Swagger UI
            "/swagger-ui.html",
            "/v3/api-docs/**" // Swagger API 문서 JSON
    };
}