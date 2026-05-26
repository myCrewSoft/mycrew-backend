package com.mycrewsoft.security.rbac;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.mycrewsoft.security.users.AuthSession;

import lombok.RequiredArgsConstructor;

/**
 * 로그인 성공 후 Redis에 저장할 인증 세션을 생성하는 팩토리.
 *
 * 역할:
 * - EMP_ID, 세션 ID, 권한 버전, authorities, scopedPermissions를 하나의 AuthSession으로 묶는다.
 * - 로그인 시점의 권한 정보를 Redis 세션에 적재한다.
 *
 * 설계 기준:
 * - Access Token에는 최소 식별 정보와 권한 버전만 담는다.
 * - 실제 scoped permission 목록은 Redis 세션에 저장한다.
 */
@Component
@RequiredArgsConstructor
public class AuthSessionFactory {

    private final RbacPermissionService rbacPermissionService;

    public AuthSession createSession(
            String sessionId,
            Long empId,
            String username,
            boolean enabled,
            Integer authVersion,
            Set<String> authorities,
            String refreshTokenHash) {
        AuthSession session = new AuthSession(
                sessionId,
                empId,
                username,
                enabled,
                authVersion,
                authorities == null ? new LinkedHashSet<>() : new LinkedHashSet<>(authorities),
                refreshTokenHash);
        session.setScopedPermissions(rbacPermissionService.loadScopedPermissions(empId));
        return session;
    }
}
