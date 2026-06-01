package com.mycrewsoft.security.rbac;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class RbacAuthorizationChangeServiceTest {

    @Test
    void refreshEmployeePermissionsIncrementsAuthVersionWithoutDeletingSessions() {
        FakeRbacMapper mapper = new FakeRbacMapper();
        RbacAuthorizationChangeService service = new RbacAuthorizationChangeService(mapper);

        service.refreshEmployeePermissions(100L);

        assertThat(mapper.incrementedEmpIds).containsExactly(100L);
    }

    @Test
    void refreshRolePermissionsIncrementsAuthVersionForAllEmployeesAssignedToRole() {
        FakeRbacMapper mapper = new FakeRbacMapper();
        mapper.roleEmpIds = List.of(100L, 101L);
        RbacAuthorizationChangeService service = new RbacAuthorizationChangeService(mapper);

        service.refreshRolePermissions(5L);

        assertThat(mapper.requestedRoleId).isEqualTo(5L);
        assertThat(mapper.incrementedEmpIds).containsExactly(100L, 101L);
    }

    private static class FakeRbacMapper implements RbacMapper {

        private final List<Long> incrementedEmpIds = new ArrayList<>();
        private List<Long> roleEmpIds = List.of();
        private Long requestedRoleId;

        @Override
        public List<com.mycrewsoft.security.authz.ScopedPermission> selectScopedPermissionsByEmpId(Long empId) {
            return List.of();
        }

        @Override
        public int incrementAuthVersionForEmpId(Long empId) {
            incrementedEmpIds.add(empId);
            return 1;
        }

        @Override
        public List<Long> selectEnabledEmpIdsByRoleId(Long roleId) {
            this.requestedRoleId = roleId;
            return roleEmpIds;
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
