package com.mycrewsoft.security.authz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.users.AuthorizationUserDetails;

class AuthorizationServiceTest {

    private final AuthorizationService authorizationService = new AuthorizationService();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void globalScopeAllowsMatchingPermissionForAnyResource() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.BOARD)
                .resourceId("55")
                .deptCd("11")
                .build();
        List<ScopedPermission> permissions = List.of(
                ScopedPermission.of("BOARD:DELETE", 1L, "system admin", ScopeType.GLOBAL, "*"));

        assertThat(authorizationService.hasPermission(100L, permissions, "BOARD:DELETE", resource)).isTrue();
    }

    @Test
    void deptScopeAllowsOnlyMatchingDepartment() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.BOARD)
                .resourceId("55")
                .deptCd("10")
                .build();
        List<ScopedPermission> permissions = List.of(
                ScopedPermission.of("BOARD:DELETE", 2L, "board manager", ScopeType.DEPT, "10"));

        assertThat(authorizationService.hasPermission(100L, permissions, "BOARD:DELETE", resource)).isTrue();
    }

    @Test
    void deptScopeDeniesDifferentDepartment() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.BOARD)
                .resourceId("56")
                .deptCd("11")
                .build();
        List<ScopedPermission> permissions = List.of(
                ScopedPermission.of("BOARD:DELETE", 2L, "board manager", ScopeType.DEPT, "10"));

        assertThat(authorizationService.hasPermission(100L, permissions, "BOARD:DELETE", resource)).isFalse();
    }

    @Test
    void projectScopeAllowsOnlyMatchingProject() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.TASK)
                .resourceId("800")
                .projId("300")
                .build();
        List<ScopedPermission> permissions = List.of(
                ScopedPermission.of("TASK:UPDATE", 3L, "project manager", ScopeType.PROJECT, "300"));

        assertThat(authorizationService.hasPermission(100L, permissions, "TASK:UPDATE", resource)).isTrue();
    }

    @Test
    void selfScopeAllowsOwnedResource() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.EMPLOYEE)
                .resourceId("100")
                .ownerEmpId(100L)
                .build();
        List<ScopedPermission> permissions = List.of(
                ScopedPermission.of("EMPLOYEE:UPDATE", 4L, "employee", ScopeType.SELF, "*"));

        assertThat(authorizationService.hasPermission(100L, permissions, "EMPLOYEE:UPDATE", resource)).isTrue();
    }

    @Test
    void selfScopeDeniesOtherOwner() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.EMPLOYEE)
                .resourceId("101")
                .ownerEmpId(101L)
                .build();
        List<ScopedPermission> permissions = List.of(
                ScopedPermission.of("EMPLOYEE:UPDATE", 4L, "employee", ScopeType.SELF, "*"));

        assertThat(authorizationService.hasPermission(100L, permissions, "EMPLOYEE:UPDATE", resource)).isFalse();
    }

    @Test
    void assertPermissionThrowsAccessDeniedWhenPermissionIsMissing() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.BOARD)
                .resourceId("55")
                .deptCd("10")
                .build();

        assertThatThrownBy(() -> authorizationService.assertPermission(100L, List.of(), "BOARD:DELETE", resource))
                .isInstanceOf(CustomException.class)
                .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void hasCurrentUserPermissionUsesSecurityContextPrincipalPermissions() {
        setAuthenticatedUserWithPermission(
                ScopedPermission.of("BOARD:DELETE", 2L, "board manager", ScopeType.DEPT, "10"));
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.BOARD)
                .resourceId("55")
                .deptCd("10")
                .build();

        assertThat(authorizationService.hasCurrentUserPermission("BOARD:DELETE", resource)).isTrue();
    }

    @Test
    void assertCurrentUserPermissionThrowsAccessDeniedForWrongScope() {
        setAuthenticatedUserWithPermission(
                ScopedPermission.of("BOARD:DELETE", 2L, "board manager", ScopeType.DEPT, "10"));
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.BOARD)
                .resourceId("56")
                .deptCd("11")
                .build();

        assertThatThrownBy(() -> authorizationService.assertCurrentUserPermission("BOARD:DELETE", resource))
                .isInstanceOf(CustomException.class)
                .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    private void setAuthenticatedUserWithPermission(ScopedPermission scopedPermission) {
        AuthorizationUserDetails principal = new AuthorizationUserDetails(
                100L,
                "user-100",
                null,
                true,
                1,
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                List.of(scopedPermission));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
