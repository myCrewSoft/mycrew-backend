//package com.mycrewsoft.security.authz;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//import java.util.List;
//import java.util.Set;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import com.mycrewsoft.common.exception.CustomException;
//import com.mycrewsoft.common.exception.ErrorCode;
//import com.mycrewsoft.security.users.AuthorizationUserDetails;
//
//class AuthorizationServiceTest {
//
//    private final AuthorizationService authorizationService = new AuthorizationService();
//
//    @AfterEach
//    void clearContext() {
//        SecurityContextHolder.clearContext();
//    }
//
//    @Test
//    void globalScopeAllowsMatchingPermissionForAnyResource() {
//        ResourceContext resource = ResourceContext.builder()
//                .resourceType(ResourceType.BOARD)
//                .resourceId("55")
//                .deptCd("11")
//                .build();
//        List<ScopedPermission> permissions = List.of(
//                ScopedPermission.of("BOARD_POST_DELETE", 1L, "system admin", ScopeType.GLOBAL, "*"));
//
//        assertThat(authorizationService.canAccess(
//                100L, permissions, PermissionCode.BOARD_POST_DELETE, resource)).isTrue();
//    }
//
//    @Test
//    void deptScopeAllowsOnlyMatchingDepartment() {
//        ResourceContext resource = ResourceContext.builder()
//                .resourceType(ResourceType.BOARD)
//                .resourceId("55")
//                .deptCd("10")
//                .build();
//        List<ScopedPermission> permissions = List.of(
//                ScopedPermission.of("BOARD_POST_DELETE", 2L, "board manager", ScopeType.DEPT, "10"));
//
//        assertThat(authorizationService.canAccess(
//                100L, permissions, PermissionCode.BOARD_POST_DELETE, resource)).isTrue();
//    }
//
//    @Test
//    void deptScopeDeniesDifferentDepartment() {
//        ResourceContext resource = ResourceContext.builder()
//                .resourceType(ResourceType.BOARD)
//                .resourceId("56")
//                .deptCd("11")
//                .build();
//        List<ScopedPermission> permissions = List.of(
//                ScopedPermission.of("BOARD_POST_DELETE", 2L, "board manager", ScopeType.DEPT, "10"));
//
//        assertThat(authorizationService.canAccess(
//                100L, permissions, PermissionCode.BOARD_POST_DELETE, resource)).isFalse();
//    }
//
//    @Test
//    void projectScopeAllowsOnlyMatchingProject() {
//        ResourceContext resource = ResourceContext.builder()
//                .resourceType(ResourceType.TASK)
//                .resourceId("800")
//                .projId("300")
//                .build();
//        List<ScopedPermission> permissions = List.of(
//                ScopedPermission.of("PROJECT_UPDATE", 3L, "project manager", ScopeType.PROJECT, "300"));
//
//        assertThat(authorizationService.canAccess(
//                100L, permissions, PermissionCode.PROJECT_UPDATE, resource)).isTrue();
//    }
//
//    @Test
//    void selfScopeAllowsOwnedResource() {
//        ResourceContext resource = ResourceContext.builder()
//                .resourceType(ResourceType.EMPLOYEE)
//                .resourceId("100")
//                .ownerEmpId(100L)
//                .build();
//        List<ScopedPermission> permissions = List.of(
//                ScopedPermission.of("MAIL_READ", 4L, "employee", ScopeType.SELF, "*"));
//
//        assertThat(authorizationService.canAccess(
//                100L, permissions, PermissionCode.MAIL_READ, resource)).isTrue();
//    }
//
//    @Test
//    void selfScopeDeniesOtherOwner() {
//        ResourceContext resource = ResourceContext.builder()
//                .resourceType(ResourceType.EMPLOYEE)
//                .resourceId("101")
//                .ownerEmpId(101L)
//                .build();
//        List<ScopedPermission> permissions = List.of(
//                ScopedPermission.of("MAIL_READ", 4L, "employee", ScopeType.SELF, "*"));
//
//        assertThat(authorizationService.canAccess(
//                100L, permissions, PermissionCode.MAIL_READ, resource)).isFalse();
//    }
//
//    @Test
//    void assertCanAccessThrowsAccessDeniedWhenPermissionIsMissing() {
//        ResourceContext resource = ResourceContext.builder()
//                .resourceType(ResourceType.BOARD)
//                .resourceId("55")
//                .deptCd("10")
//                .build();
//
//        assertThatThrownBy(() -> authorizationService.assertCanAccess(
//                100L, List.of(), PermissionCode.BOARD_POST_DELETE, resource))
//                .isInstanceOf(CustomException.class)
//                .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
//                        .isEqualTo(ErrorCode.ACCESS_DENIED));
//    }
//
//    @Test
//    void canAccessUsesSecurityContextPrincipalPermissions() {
//        setAuthenticatedUserWithPermission(
//                ScopedPermission.of("BOARD_POST_DELETE", 2L, "board manager", ScopeType.DEPT, "10"));
//        ResourceContext resource = ResourceContext.builder()
//                .resourceType(ResourceType.BOARD)
//                .resourceId("55")
//                .deptCd("10")
//                .build();
//
//        assertThat(authorizationService.canAccess(PermissionCode.BOARD_POST_DELETE, resource)).isTrue();
//    }
//
//    @Test
//    void assertCanAccessThrowsAccessDeniedForWrongScope() {
//        setAuthenticatedUserWithPermission(
//                ScopedPermission.of("BOARD_POST_DELETE", 2L, "board manager", ScopeType.DEPT, "10"));
//        ResourceContext resource = ResourceContext.builder()
//                .resourceType(ResourceType.BOARD)
//                .resourceId("56")
//                .deptCd("11")
//                .build();
//
//        assertThatThrownBy(() -> authorizationService.assertCurrentUserPermission(
//                PermissionCode.BOARD_POST_DELETE, resource))
//                .isInstanceOf(CustomException.class)
//                .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
//                        .isEqualTo(ErrorCode.ACCESS_DENIED));
//    }
//
//    @Test
//    void getPermissionScopesCollectsOnlyRequestedPermissionScopes() {
//        List<ScopedPermission> permissions = List.of(
//                ScopedPermission.of("BOARD_POST_READ", 1L, "employee", ScopeType.DEPT, "HR"),
//                ScopedPermission.of("BOARD_POST_READ", 2L, "cross dept", ScopeType.DEPT, "ACCOUNTING"),
//                ScopedPermission.of("BOARD_POST_READ", 3L, "project", ScopeType.PROJECT, "300"),
//                ScopedPermission.of("BOARD_POST_DELETE", 4L, "other", ScopeType.DEPT, "DEV"));
//
//        PermissionScopeSet scopeSet = authorizationService.getPermissionScopes(
//                permissions, PermissionCode.BOARD_POST_READ);
//
//        assertThat(scopeSet.hasGlobal()).isFalse();
//        assertThat(scopeSet.getDepartmentScopeIds()).containsExactly("HR", "ACCOUNTING");
//        assertThat(scopeSet.getProjectScopeIds()).containsExactly("300");
//    }
//
//    @Test
//    void getCurrentPermissionScopesReadsSecurityContextPrincipalPermissions() {
//        AuthorizationUserDetails principal = new AuthorizationUserDetails(
//                100L,
//                "user-100",
//                null,
//                true,
//                1,
//                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
//                List.of(
//                        ScopedPermission.of("BOARD_POST_READ", 1L, "employee", ScopeType.DEPT, "HR"),
//                        ScopedPermission.of("BOARD_POST_READ", 2L, "admin", ScopeType.GLOBAL, "*")));
//        SecurityContextHolder.getContext().setAuthentication(
//                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
//
//        PermissionScopeSet scopeSet = authorizationService.getCurrentPermissionScopes(PermissionCode.BOARD_POST_READ);
//
//        assertThat(scopeSet.hasGlobal()).isTrue();
//        assertThat(scopeSet.getDepartmentScopeIds()).containsExactly("HR");
//    }
//
//    private void setAuthenticatedUserWithPermission(ScopedPermission scopedPermission) {
//        AuthorizationUserDetails principal = new AuthorizationUserDetails(
//                100L,
//                "user-100",
//                null,
//                true,
//                1,
//                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
//                List.of(scopedPermission));
//        SecurityContextHolder.getContext().setAuthentication(
//                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
//    }
//}
