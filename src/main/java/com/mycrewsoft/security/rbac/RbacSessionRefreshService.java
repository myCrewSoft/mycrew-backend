package com.mycrewsoft.security.rbac;

import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.security.users.AuthSession;

import lombok.RequiredArgsConstructor;

/**
 * 요청 처리 중 Redis 세션의 권한 정보가 오래되었는지 확인하고 갱신하는 서비스.
 *
 * 동작 흐름:
 * - Redis 세션의 authVersion을 읽는다.
 * - DB의 최신 authVersion과 비교한다.
 * - DB 버전이 더 높으면 authorities와 scopedPermissions를 다시 조회한다.
 * - 갱신된 AuthSession을 Redis에 저장한다.
 *
 * 목적:
 * - 사용자가 다시 로그인하지 않아도 변경된 권한이 다음 요청부터 반영되도록 한다.
 */
@Service
@RequiredArgsConstructor
public class RbacSessionRefreshService {

    private final RbacMapper rbacMapper;
    private final RbacPermissionService rbacPermissionService;
    private final AuthSessionStore authSessionStore;

    @Transactional(readOnly = true)
    public AuthSession refreshIfStale(AuthSession session) {
        if (session == null || session.getEmpId() == null) {
            return session;
        }

        Integer latestAuthVersion = rbacMapper.selectAuthVersionByEmpId(session.getEmpId());
        int latestVersion = latestAuthVersion == null ? 0 : latestAuthVersion;
        int sessionVersion = session.getAuthVersion() == null ? 0 : session.getAuthVersion();

        if (sessionVersion >= latestVersion) {
            return session;
        }

        List<String> authorities = rbacMapper.selectAuthoritiesByEmpId(session.getEmpId());
        session.setAuthorities(new LinkedHashSet<>(authorities == null ? List.of() : authorities));
        session.setScopedPermissions(rbacPermissionService.loadScopedPermissions(session.getEmpId()));
        session.setAuthVersion(latestVersion);
        authSessionStore.updateSession(session);
        return session;
    }
}
