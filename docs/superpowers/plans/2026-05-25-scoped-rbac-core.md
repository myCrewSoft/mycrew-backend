# Scoped RBAC Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the scoped authorization core for dynamic `GLOBAL`, `DEPT`, `PROJECT`, and `SELF` permissions.

**Architecture:** Add small security authorization model classes, extend the Redis-backed session principal with scoped permissions, and implement an `AuthorizationService` that checks permission code plus resource scope. This first slice does not add database mappers or migrations because the current project has no login/RBAC loading flow yet.

**Tech Stack:** Java 21, Spring Boot 3.5, Spring Security, JUnit 5, AssertJ, Lombok.

---

## File Structure

- Create `src/main/java/com/mycrewsoft/security/authz/ScopeType.java`: enum for supported scope types.
- Create `src/main/java/com/mycrewsoft/security/authz/ResourceType.java`: enum for resource types used in authorization checks.
- Create `src/main/java/com/mycrewsoft/security/authz/ScopedPermission.java`: value object cached in Redis session and principal.
- Create `src/main/java/com/mycrewsoft/security/authz/ResourceContext.java`: value object describing the target resource.
- Create `src/main/java/com/mycrewsoft/security/authz/AuthorizationService.java`: scope-aware permission checker.
- Modify `src/main/java/com/mycrewsoft/security/users/AuthSession.java`: add `scopedPermissions`.
- Modify `src/main/java/com/mycrewsoft/security/users/AuthorizationUserDetails.java`: add `scopedPermissions`.
- Modify `src/main/java/com/mycrewsoft/security/jwt/JwtAuthenticationFilter.java`: copy scoped permissions from Redis session into the principal.
- Modify `src/main/java/com/mycrewsoft/common/exception/ErrorCode.java`: add `INVALID_SCOPE`.
- Create `src/test/java/com/mycrewsoft/security/authz/AuthorizationServiceTest.java`: unit tests for scope matching.
- Modify `src/test/java/com/mycrewsoft/security/service/RefreshTokenServiceRedisIntegrationTest.java`: keep constructors compatible with new `AuthSession` field.

### Task 1: Authorization Model and Service

**Files:**
- Create: `src/test/java/com/mycrewsoft/security/authz/AuthorizationServiceTest.java`
- Create: `src/main/java/com/mycrewsoft/security/authz/ScopeType.java`
- Create: `src/main/java/com/mycrewsoft/security/authz/ResourceType.java`
- Create: `src/main/java/com/mycrewsoft/security/authz/ScopedPermission.java`
- Create: `src/main/java/com/mycrewsoft/security/authz/ResourceContext.java`
- Create: `src/main/java/com/mycrewsoft/security/authz/AuthorizationService.java`

- [ ] **Step 1: Write failing tests for authorization behavior**

Create `AuthorizationServiceTest` with tests for:

```java
package com.mycrewsoft.security.authz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;

class AuthorizationServiceTest {

    private final AuthorizationService authorizationService = new AuthorizationService();

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
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\mvnw.cmd -q -Dtest=AuthorizationServiceTest test`

Expected: fails to compile because `AuthorizationService`, `ScopedPermission`, `ScopeType`, `ResourceType`, and `ResourceContext` do not exist.

- [ ] **Step 3: Implement authorization model and service**

Create the production classes with the APIs used by the test. `AuthorizationService.hasPermission` checks `permCd` first, then applies scope matching:

```text
GLOBAL -> true
DEPT -> scopeId equals resource.deptCd
PROJECT -> scopeId equals resource.projId
SELF -> current emp id equals resource.ownerEmpId
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\mvnw.cmd -q -Dtest=AuthorizationServiceTest test`

Expected: all `AuthorizationServiceTest` tests pass.

### Task 2: Session and Principal Scoped Permissions

**Files:**
- Modify: `src/main/java/com/mycrewsoft/security/users/AuthSession.java`
- Modify: `src/main/java/com/mycrewsoft/security/users/AuthorizationUserDetails.java`
- Modify: `src/main/java/com/mycrewsoft/security/jwt/JwtAuthenticationFilter.java`
- Modify: `src/test/java/com/mycrewsoft/security/service/RefreshTokenServiceRedisIntegrationTest.java`

- [ ] **Step 1: Write failing filter test or update existing Redis integration expectations**

Add assertions in `createsAuthenticationWithAuthoritiesLoadedFromRedisSession` that a scoped permission added to `AuthSession` is present on `AuthorizationUserDetails`.

- [ ] **Step 2: Run test to verify it fails**

Run: `.\mvnw.cmd -q -Dtest=RefreshTokenServiceRedisIntegrationTest#createsAuthenticationWithAuthoritiesLoadedFromRedisSession test`

Expected: compile failure or assertion failure because `AuthorizationUserDetails` does not expose scoped permissions yet.

- [ ] **Step 3: Extend session and principal**

Add `List<ScopedPermission> scopedPermissions` to `AuthSession` and `AuthorizationUserDetails`, defaulting to an empty list. Update `JwtAuthenticationFilter.createUserDetails` to copy the list from session to principal.

- [ ] **Step 4: Run test to verify it passes**

Run: `.\mvnw.cmd -q -Dtest=RefreshTokenServiceRedisIntegrationTest#createsAuthenticationWithAuthoritiesLoadedFromRedisSession test`

Expected: test passes when Redis is available; if Redis is unavailable, the test aborts by its existing assumption.

### Task 3: Security Config and Error Code Cleanup

**Files:**
- Modify: `src/main/java/com/mycrewsoft/config/SecurityConfig.java`
- Modify: `src/main/java/com/mycrewsoft/common/exception/ErrorCode.java`

- [ ] **Step 1: Add focused assertion by inspecting current config behavior**

No new web integration test is added in this slice because the application has no implemented protected controllers. The change is a narrow configuration update that aligns with the approved design.

- [ ] **Step 2: Change SecurityConfig request authorization**

Replace the current global `.anyRequest().hasRole(Constants.PRIMARY_ADMIN)` rule with `.anyRequest().authenticated()`.

- [ ] **Step 3: Add INVALID_SCOPE error code**

Add `INVALID_SCOPE(HttpStatus.BAD_REQUEST, "AUTH_008", "Invalid authorization scope.")` to `ErrorCode`.

- [ ] **Step 4: Run core tests**

Run: `.\mvnw.cmd -q -Dtest=AuthorizationServiceTest,JwtTokenProviderTest test`

Expected: all tests pass.

## Self-Review

- Spec coverage: this plan implements the scoped permission model, scope matching rules, Redis session shape, principal propagation, and URL-level authentication rule. Database rename/migration and RBAC loading queries are intentionally left for a later slice because the current repository does not have login/RBAC mapper code.
- Placeholder scan: no placeholder steps are used.
- Type consistency: `ScopeType`, `ResourceType`, `ScopedPermission`, `ResourceContext`, and `AuthorizationService` are named consistently across tasks.
