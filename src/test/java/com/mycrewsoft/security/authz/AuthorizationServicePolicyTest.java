package com.mycrewsoft.security.authz;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mycrewsoft.common.constant.PermissionCode;

class AuthorizationServicePolicyTest {

    @Test
    void boardReadAllowsBasicEmployeeInOwnDepartmentWithoutStoredDeptScope() {
        FakeAuthorizationRelationshipResolver relationships = new FakeAuthorizationRelationshipResolver();
        relationships.sameDepartment = true;
        AuthorizationService authorizationService = new AuthorizationService(relationships);

        boolean canAccess = authorizationService.canAccess(
                1111L,
                List.of(ScopedPermission.of(
                        PermissionCode.BOARD_POST_READ.getCode(),
                        1L,
                        "basic employee",
                        ScopeType.SELF,
                        "1111")),
                PermissionCode.BOARD_POST_READ,
                ResourceContext.builder()
                        .resourceType(ResourceType.BOARD)
                        .deptCd("DEPT_024")
                        .build());

        assertThat(canAccess).isTrue();
    }

    @Test
    void boardCreateDoesNotUseSelfOwnerScopeToBypassDifferentDepartmentTarget() {
        FakeAuthorizationRelationshipResolver relationships = new FakeAuthorizationRelationshipResolver();
        relationships.sameDepartment = false;
        AuthorizationService authorizationService = new AuthorizationService(relationships);

        boolean canAccess = authorizationService.canAccess(
                1111L,
                List.of(ScopedPermission.of(
                        PermissionCode.BOARD_POST_CREATE.getCode(),
                        1L,
                        "basic employee",
                        ScopeType.SELF,
                        "1111")),
                PermissionCode.BOARD_POST_CREATE,
                ResourceContext.builder()
                        .resourceType(ResourceType.BOARD)
                        .ownerEmpId(1111L)
                        .deptCd("DEPT_010")
                        .build());

        assertThat(canAccess).isFalse();
    }

    @Test
    void emptyResourceContextActsAsFeatureGateWhenPermissionExists() {
        AuthorizationService authorizationService = new AuthorizationService(
                new FakeAuthorizationRelationshipResolver());

        boolean canAccess = authorizationService.canAccess(
                1111L,
                List.of(ScopedPermission.of(
                        PermissionCode.SCHEDULE_READ.getCode(),
                        1L,
                        "basic employee",
                        ScopeType.SELF,
                        "1111")),
                PermissionCode.SCHEDULE_READ,
                ResourceContext.builder()
                        .resourceType(ResourceType.SCHEDULE)
                        .build());

        assertThat(canAccess).isTrue();
    }

    @Test
    void explicitDepartmentDelegationStillAllowsDepartmentScopedBoardRead() {
        AuthorizationService authorizationService = new AuthorizationService(
                new FakeAuthorizationRelationshipResolver());

        boolean canAccess = authorizationService.canAccess(
                1111L,
                List.of(ScopedPermission.of(
                        PermissionCode.BOARD_POST_READ.getCode(),
                        10L,
                        "department board manager",
                        ScopeType.DEPT,
                        "DEPT_024")),
                PermissionCode.BOARD_POST_READ,
                ResourceContext.builder()
                        .resourceType(ResourceType.BOARD)
                        .deptCd("DEPT_024")
                        .build());

        assertThat(canAccess).isTrue();
    }

    @Test
    void selfScopeStillAllowsOwnerScopedPersonalResources() {
        AuthorizationService authorizationService = new AuthorizationService(
                new FakeAuthorizationRelationshipResolver());

        boolean canAccess = authorizationService.canAccess(
                1111L,
                List.of(ScopedPermission.of(
                        PermissionCode.MAIL_READ.getCode(),
                        1L,
                        "basic employee",
                        ScopeType.SELF,
                        "1111")),
                PermissionCode.MAIL_READ,
                ResourceContext.builder()
                        .resourceType(ResourceType.MAIL)
                        .ownerEmpId(1111L)
                        .build());

        assertThat(canAccess).isTrue();
    }

    private static class FakeAuthorizationRelationshipResolver implements AuthorizationRelationshipResolver {

        private boolean sameDepartment;

        @Override
        public boolean isSameDepartment(Long empId, String deptCd) {
            return sameDepartment;
        }

        @Override
        public boolean isProjectMember(Long empId, String projId) {
            return false;
        }

        @Override
        public boolean isProjectLeader(Long empId, String projId) {
            return false;
        }
    }
}
