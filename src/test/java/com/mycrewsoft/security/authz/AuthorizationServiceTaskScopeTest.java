package com.mycrewsoft.security.authz;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class AuthorizationServiceTaskScopeTest {

    private final AuthorizationService authorizationService = new AuthorizationService();

    @Test
    void canAccessReturnsTrueWhenTaskScopeMatchesResourceTaskId() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.SCHEDULE)
                .taskId("5009")
                .projId("1001")
                .build();

        boolean canAccess = authorizationService.canAccess(
                1111L,
                List.of(ScopedPermission.of(
                        PermissionCode.SCHEDULE_CREATE.getCode(),
                        1L,
                        "task schedule writer",
                        ScopeType.TASK,
                        "5009")),
                PermissionCode.SCHEDULE_CREATE,
                resource);

        assertThat(canAccess).isTrue();
    }

    @Test
    void canAccessReturnsFalseWhenTaskScopeDoesNotMatchResourceTaskId() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.SCHEDULE)
                .taskId("5010")
                .projId("1001")
                .build();

        boolean canAccess = authorizationService.canAccess(
                1111L,
                List.of(ScopedPermission.of(
                        PermissionCode.SCHEDULE_CREATE.getCode(),
                        1L,
                        "task schedule writer",
                        ScopeType.TASK,
                        "5009")),
                PermissionCode.SCHEDULE_CREATE,
                resource);

        assertThat(canAccess).isFalse();
    }

    @Test
    void canAccessKeepsProjectScopeWorkingForTaskScheduleResource() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.SCHEDULE)
                .taskId("5009")
                .projId("1001")
                .build();

        boolean canAccess = authorizationService.canAccess(
                1111L,
                List.of(ScopedPermission.of(
                        PermissionCode.SCHEDULE_CREATE.getCode(),
                        1L,
                        "project schedule writer",
                        ScopeType.PROJECT,
                        "1001")),
                PermissionCode.SCHEDULE_CREATE,
                resource);

        assertThat(canAccess).isTrue();
    }
}
