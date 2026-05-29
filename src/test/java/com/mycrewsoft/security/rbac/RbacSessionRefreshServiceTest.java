//package com.mycrewsoft.security.rbac;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.junit.jupiter.api.Test;
//
//import com.mycrewsoft.security.authz.ScopeType;
//import com.mycrewsoft.security.authz.ScopedPermission;
//import com.mycrewsoft.security.users.AuthSession;
//
//class RbacSessionRefreshServiceTest {
//
//    @Test
//    void refreshIfStaleReloadsPermissionsAuthoritiesAndAuthVersion() {
//        FakeRbacMapper mapper = new FakeRbacMapper();
//        mapper.authVersion = 4;
//        mapper.authorities = List.of("ROLE_USER", "ROLE_ADMIN");
//        mapper.permissions = List.of(
//                ScopedPermission.of("BOARD_DELETE", 10L, "board manager", ScopeType.DEPT, "D001"));
//        FakeSessionStore sessionStore = new FakeSessionStore();
//        RbacSessionRefreshService service = new RbacSessionRefreshService(
//                mapper,
//                new RbacPermissionService(mapper),
//                sessionStore);
//        AuthSession session = new AuthSession(
//                "session-1",
//                100L,
//                "user-100",
//                true,
//                3,
//                new LinkedHashSet<>(Set.of("ROLE_USER")),
//                "refresh-hash");
//
//        AuthSession refreshed = service.refreshIfStale(session);
//
//        assertThat(refreshed).isSameAs(session);
//        assertThat(session.getAuthVersion()).isEqualTo(4);
//        assertThat(session.getAuthorities()).containsExactly("ROLE_USER", "ROLE_ADMIN");
//        assertThat(session.getScopedPermissions())
//                .extracting(ScopedPermission::getPermCd, ScopedPermission::getScopeType, ScopedPermission::getScopeId)
//                .containsExactly(org.assertj.core.groups.Tuple.tuple("BOARD_DELETE", ScopeType.DEPT, "D001"));
//        assertThat(sessionStore.updatedSession).isSameAs(session);
//    }
//
//    @Test
//    void refreshIfStaleDoesNothingWhenSessionVersionIsCurrent() {
//        FakeRbacMapper mapper = new FakeRbacMapper();
//        mapper.authVersion = 3;
//        FakeSessionStore sessionStore = new FakeSessionStore();
//        RbacSessionRefreshService service = new RbacSessionRefreshService(
//                mapper,
//                new RbacPermissionService(mapper),
//                sessionStore);
//        AuthSession session = new AuthSession(
//                "session-1",
//                100L,
//                "user-100",
//                true,
//                3,
//                new LinkedHashSet<>(Set.of("ROLE_USER")),
//                "refresh-hash");
//
//        service.refreshIfStale(session);
//
//        assertThat(sessionStore.updatedSession).isNull();
//    }
//
//    private static class FakeSessionStore implements AuthSessionStore {
//
//        private AuthSession updatedSession;
//
//        @Override
//        public void updateSession(AuthSession session) {
//            this.updatedSession = session;
//        }
//    }
//
//    private static class FakeRbacMapper implements RbacMapper {
//
//        private Integer authVersion = 0;
//        private List<String> authorities = List.of();
//        private List<ScopedPermission> permissions = List.of();
//
//        @Override
//        public List<ScopedPermission> selectScopedPermissionsByEmpId(Long empId) {
//            return permissions;
//        }
//
//        @Override
//        public int incrementAuthVersionForEmpId(Long empId) {
//            return 0;
//        }
//
//        @Override
//        public List<Long> selectEnabledEmpIdsByRoleId(Long roleId) {
//            return List.of();
//        }
//
//        @Override
//        public Integer selectAuthVersionByEmpId(Long empId) {
//            return authVersion;
//        }
//
//        @Override
//        public List<String> selectAuthoritiesByEmpId(Long empId) {
//            return authorities;
//        }
//    }
//}
