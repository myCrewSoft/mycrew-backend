package com.mycrewsoft.security.users;

/**
 * 인증에 필요한 사용자 조회 결과를 담는 record.
 *
 * 역할:
 * - DB의 사원 계정 정보를 AuthorizationUserDetailsService로 전달한다.
 * - Spring Security UserDetails를 만들기 전 단계의 순수 조회 모델이다.
 *
 * 포함 정보:
 * - empId: 사원 번호 고유 ID
 * - username: 로그인 식별자
 * - password: 암호화된 비밀번호
 * - enabled: 계정 사용 가능 여부
 * - empStat: 사원 상태 (예: 재직, 휴직, 퇴사 등)
 */
public record AuthorizationUserRecord(
        Long empId,
        String username,
        String password,
        Boolean enabled,
        String empStat) {

    public AuthorizationUserRecord(
            Long empId,
            String username,
            String password,
            Boolean enabled) {
        this(empId, username, password, enabled, null);
    }
}
