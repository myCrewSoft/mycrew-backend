package com.mycrewsoft.security.rbac;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.mycrewsoft.security.authz.ScopeType;
import com.mycrewsoft.security.authz.ScopedPermission;
import com.mycrewsoft.security.users.AuthSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class AuthSessionFactoryTest {

    @Test
    void createSessionLoadsScopedPermissionsForLoginSession() {
        FakeRbacMapper mapper = new FakeRbacMapper(List.of(
                ScopedPermission.of("PROJECT_MANAGE", 20L, "project manager", ScopeType.PROJECT, "300")));
        AuthSessionFactory factory = new AuthSessionFactory(new RbacPermissionService(mapper));

        AuthSession session = factory.createSession(
                "session-1",
                100L,
                "user-100",
                true,
                3,
                new LinkedHashSet<>(Set.of("ROLE_USER")),
                "refresh-token");

        assertThat(session.getSessionId()).isEqualTo("session-1");
        assertThat(session.getEmpId()).isEqualTo(100L);
        assertThat(session.getAuthVersion()).isEqualTo(3);
        assertThat(session.getAuthorities()).containsExactly("ROLE_USER");
        assertThat(session.getRefreshTokenHash()).isEqualTo("refresh-token");
        assertThat(session.getScopedPermissions())
                .extracting(ScopedPermission::getPermCd, ScopedPermission::getScopeType, ScopedPermission::getScopeId)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("PROJECT_MANAGE", ScopeType.PROJECT, "300"));
        log.info("Created AuthSession: {}", session.getSessionId());
        log.info("Created getAuthorities: {}", session.getAuthorities());
        session.getScopedPermissions()
        	.forEach(loggedPermission -> log.info("ScopedPermission: {}", loggedPermission.getPermCd()
        					+ ", " + loggedPermission.getScopeType() + ", " + loggedPermission.getScopeId()));
    }

    private static class FakeRbacMapper implements RbacMapper {

        private final List<ScopedPermission> permissions;

        private FakeRbacMapper(List<ScopedPermission> permissions) {
            this.permissions = permissions;
        }

        @Override
        public List<ScopedPermission> selectScopedPermissionsByEmpId(Long empId) {
            return permissions;
        }

        @Override
        public int incrementAuthVersionForEmpId(Long empId) {
            return 0;
        }

        @Override
        public List<Long> selectEnabledEmpIdsByRoleId(Long roleId) {
            return List.of();
        }

        @Override
        public Integer selectAuthVersionByEmpId(Long empId) {
            return 0;
        }

        @Override
        public List<String> selectAuthoritiesByEmpId(Long empId) {
            return List.of();
        }
    }
}
