package com.mycrewsoft.security.rbac;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mycrewsoft.security.authz.ScopeType;
import com.mycrewsoft.security.authz.ScopedPermission;

class RbacPermissionServiceTest {

    @Test
    void loadScopedPermissionsReturnsPermissionsFromMapper() {
        FakeRbacMapper mapper = new FakeRbacMapper(List.of(
                ScopedPermission.of("BOARD_DELETE", 10L, "board manager", ScopeType.DEPT, "D001")));
        RbacPermissionService service = new RbacPermissionService(mapper);

        List<ScopedPermission> permissions = service.loadScopedPermissions(100L);

        assertThat(mapper.requestedEmpId).isEqualTo(100L);
        assertThat(permissions)
                .extracting(ScopedPermission::getPermCd, ScopedPermission::getRoleId,
                        ScopedPermission::getScopeType, ScopedPermission::getScopeId)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("BOARD_DELETE", 10L, ScopeType.DEPT, "D001"));
    }

    @Test
    void loadScopedPermissionsReturnsEmptyListWhenMapperReturnsNull() {
        RbacPermissionService service = new RbacPermissionService(new FakeRbacMapper(null));

        assertThat(service.loadScopedPermissions(100L)).isEmpty();
    }

    private static class FakeRbacMapper implements RbacMapper {

        private final List<ScopedPermission> permissions;
        private Long requestedEmpId;

        private FakeRbacMapper(List<ScopedPermission> permissions) {
            this.permissions = permissions;
        }

        @Override
        public List<ScopedPermission> selectScopedPermissionsByEmpId(Long empId) {
            this.requestedEmpId = empId;
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
