package com.mycrewsoft.security.users;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.mycrewsoft.security.authz.ScopedPermission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Redis에 저장되는 인증 세션 모델.
 *
 * 역할:
 * - 로그인한 사원의 세션 상태를 서버 측에서 관리한다.
 * - Access Token만으로 담기 어려운 권한 목록과 세션 메타데이터를 저장한다.
 *
 * 포함 정보:
 * - sessionId: Redis 세션 식별자
 * - empId: 사원 ID(EMP_ID)
 * - authVersion: 권한 버전
 * - authorities: Spring Security 전역 권한
 * - scopedPermissions: 범위 기반 권한 목록
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthSession {

    private String sessionId;

    @JsonAlias("userId")
    private Long empId;

    private String username;
    private boolean enabled;
    private Integer authVersion;
    private boolean exec;
    private Set<String> authorities = new LinkedHashSet<>();
    private List<ScopedPermission> scopedPermissions = new ArrayList<>();
    private String refreshTokenHash;

    public AuthSession(
            String sessionId,
            Long empId,
            String username,
            boolean enabled,
            Integer authVersion,
            boolean exec,
            Set<String> authorities,
            String refreshTokenHash) {
        this.sessionId = sessionId;
        this.empId = empId;
        this.username = username;
        this.enabled = enabled;
        this.authVersion = authVersion;
        this.exec = exec;
        this.authorities = authorities;
        this.refreshTokenHash = refreshTokenHash;
    }
}
