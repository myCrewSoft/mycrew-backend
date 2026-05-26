# Scoped RBAC Design

## Context

The current authentication model uses JWT access and refresh tokens. Both tokens include the user id, auth version, token type, and Redis session id. Redis stores the server-side `AuthSession`, including the enabled flag, auth version, refresh token hash, and simple Spring Security authorities.

This is enough for global role checks such as `ROLE_ADMIN`, but it cannot express scoped permissions such as "user A can manage board posts only for department 10".

The ERD already has the base tables for RBAC:

- `TB_EMPLOYEE`
- `TB_DEPARTMENT`
- `TB_ROLE`
- `TB_PERMISSION`
- `TB_PERMISSION_ROLE_MAPPING`
- `TB_ROLE_MEMBER_MAPPING`
- `TB_AUTH_VERSION`

The ERD also has resource scope columns that can drive dynamic authorization:

- `TB_BOARD.DEPT_CD`, `TB_BOARD.PROJ_ID`
- `TB_INTG_SCHD.DEPT_CD`, `TB_INTG_SCHD.PROJ_ID`
- `TB_PROJECT.PROJ_ID`, `PROJ_MGR_MBR_ID`, `PROJ_LDR_MBR_ID`
- `TB_PROJ_MEMBER.EMP_ID`, `TB_PROJ_MEMBER.PROJ_ID`
- `TB_TASK.PROJ_ID`, `TASK_MNGR_ID`
- `TB_TASK_MEMBER.MBR_ID`, `TASK_ID`, `PROJ_ID`
- `TB_APRVL_DOC.DRFTR_MBR_ID`
- `TB_APRVL_LINE.APRVR_MBR_ID`
- `TB_DRIVE.EMP_ID`
- `TB_PROJ_DRIVE.PROJ_ID`, `TASK_ID`

## Decision

Use scoped RBAC.

Roles and permissions remain reusable global definitions. Scope is applied when a role is assigned to a user.

Example:

```text
Employee 100
Role: board manager
Scope type: DEPT
Scope id: 10
```

This means employee 100 can use the board manager role only for department 10 resources.

## Database Model

Rename `TB_ROLE_MEMBER_MAPPING` to `TB_ROLE_ASSIGNMENT` and expand it from a simple many-to-many mapping into the single source of truth for role grants.

```text
TB_ROLE_ASSIGNMENT
  ROLE_ASSIGN_ID   NUMBER(22)    PK
  EMP_ID           NUMBER(10)    NOT NULL
  ROLE_ID          NUMBER(22)    NOT NULL
  SCOPE_TYPE_CD    VARCHAR2(20)  NOT NULL
  SCOPE_ID         VARCHAR2(50)  NOT NULL
  ENABLED          CHAR(1)       DEFAULT 'Y'
  FRST_REG_DT      DATE
  FRST_RGTR_ID     NUMBER(10)
  LAST_MDFCN_DT    DATE
  LAST_MDFR_ID     NUMBER(10)
```

Constraints:

```text
PK: ROLE_ASSIGN_ID
FK: EMP_ID -> TB_EMPLOYEE.EMP_ID
FK: ROLE_ID -> TB_ROLE.ROLE_ID
UK: EMP_ID, ROLE_ID, SCOPE_TYPE_CD, SCOPE_ID
CHECK: SCOPE_TYPE_CD IN ('GLOBAL', 'DEPT', 'PROJECT', 'SELF')
CHECK: ENABLED IN ('Y', 'N')
```

`SCOPE_ID` is stored as text because different scope types use different physical key types. `DEPT` uses `DEPT_CD`, while `PROJECT` uses `PROJ_ID`. The application validates that the target scope exists when role assignments are created or updated.

Use `SCOPE_ID='*'` for `GLOBAL` and `SELF` to keep uniqueness simple.

```text
GLOBAL  -> SCOPE_ID='*'
SELF    -> SCOPE_ID='*'
DEPT    -> SCOPE_ID='<DEPT_CD>'
PROJECT -> SCOPE_ID='<PROJ_ID>'
```

Existing `TB_ROLE_MEMBER_MAPPING` rows migrate as global role assignments.

```text
old: EMP_ID=100, ROLE_ID=5
new: EMP_ID=100, ROLE_ID=5, SCOPE_TYPE_CD='GLOBAL', SCOPE_ID='*', ENABLED='Y'
```

## Permission Code Model

Permission codes use the format `DOMAIN_ACTION`.

Initial permission set:

```text
BOARD_READ
BOARD_CREATE
BOARD_UPDATE
BOARD_DELETE
BOARD_MANAGE

PROJECT_READ
PROJECT_CREATE
PROJECT_UPDATE
PROJECT_DELETE
PROJECT_MANAGE

TASK_READ
TASK_CREATE
TASK_UPDATE
TASK_DELETE
TASK_MANAGE

SCHEDULE_READ
SCHEDULE_CREATE
SCHEDULE_UPDATE
SCHEDULE_DELETE
SCHEDULE_MANAGE

DRIVE_READ
DRIVE_UPLOAD
DRIVE_UPDATE
DRIVE_DELETE
DRIVE_MANAGE

APPROVAL_READ
APPROVAL_DRAFT
APPROVAL_APPROVE
APPROVAL_MANAGE

EMPLOYEE_READ
EMPLOYEE_CREATE
EMPLOYEE_UPDATE
EMPLOYEE_DELETE
EMPLOYEE_MANAGE

ROLE_READ
ROLE_CREATE
ROLE_UPDATE
ROLE_DELETE
ROLE_ASSIGN
```

Roles are permission bundles. Scope is not stored on the role itself.

Example roles:

```text
System administrator
  Expected scope: GLOBAL
  Permissions: all operational permissions

Department board manager
  Expected scope: DEPT
  Permissions:
    BOARD_READ
    BOARD_CREATE
    BOARD_UPDATE
    BOARD_DELETE
    BOARD_MANAGE

Department schedule manager
  Expected scope: DEPT
  Permissions:
    SCHEDULE_READ
    SCHEDULE_CREATE
    SCHEDULE_UPDATE
    SCHEDULE_DELETE
    SCHEDULE_MANAGE

Project manager
  Expected scope: PROJECT
  Permissions:
    PROJECT_READ
    PROJECT_UPDATE
    PROJECT_MANAGE
    TASK_READ
    TASK_CREATE
    TASK_UPDATE
    TASK_DELETE
    TASK_MANAGE
    DRIVE_READ
    DRIVE_UPLOAD
    DRIVE_UPDATE
    DRIVE_DELETE

General employee
  Expected scope: SELF
  Permissions:
    EMPLOYEE_READ
    EMPLOYEE_UPDATE
    DRIVE_READ
    DRIVE_UPLOAD
```

The same role can be assigned with different scopes.

```text
A: board manager + DEPT 10
B: board manager + DEPT 11
C: board manager + GLOBAL
```

## Runtime Authorization

URL-level security should only distinguish public routes from authenticated routes.

```text
PUBLIC_URLS -> permitAll
all other routes -> authenticated
```

Dynamic authorization happens in the service layer, where the application can load the target resource and build a scope context.

Introduce:

```text
AuthorizationService
  boolean hasPermission(Long empId, String permCd, ResourceContext resource)
  void assertPermission(Long empId, String permCd, ResourceContext resource)
```

`ResourceContext` contains the resource attributes needed for scope matching.

```text
ResourceContext
  resourceType
  resourceId
  deptCd
  projId
  ownerEmpId
  managerEmpId
  memberEmpIds
```

Only fields relevant to a resource need to be populated.

Example board delete flow:

```text
BoardService.deleteBoard(boardId)

1. Resolve current employee id from SecurityContext.
2. Load TB_BOARD by boardId.
3. Build ResourceContext with resourceType=BOARD, resourceId, deptCd, projId, ownerEmpId.
4. Call authorizationService.assertPermission(empId, "BOARD_DELETE", context).
5. Delete only if authorization succeeds.
```

Scope matching rules:

```text
GLOBAL:
  Permission match is enough.

DEPT:
  assignment.scopeId == resource.deptCd

PROJECT:
  assignment.scopeId == resource.projId

SELF:
  resource.ownerEmpId == empId
  or target employee id == empId for personal resources
```

Implementation starts with explicit service calls:

```java
authorizationService.assertPermission(currentEmpId, "BOARD_DELETE", boardContext);
```

This is preferred over immediate `@PreAuthorize` usage because resource context construction requires domain queries. Once patterns stabilize, method-security helpers can be added.

## Redis Session Cache

Keep Redis as the request-time authorization cache. Extend `AuthSession` with scoped permissions.

```text
AuthSession
  sessionId
  userId
  username
  enabled
  authVersion
  authorities
  scopedPermissions
  refreshTokenHash
```

`authorities` remains for Spring Security compatibility.

```text
authorities:
  ROLE_USER
  ROLE_ADMIN
```

Dynamic authorization uses `scopedPermissions`.

```text
ScopedPermission
  permCd
  roleId
  roleName
  scopeType
  scopeId
```

Example:

```json
{
  "userId": 100,
  "authVersion": 3,
  "authorities": ["ROLE_USER"],
  "scopedPermissions": [
    {
      "permCd": "BOARD_READ",
      "roleId": 10,
      "roleName": "board manager",
      "scopeType": "DEPT",
      "scopeId": "10"
    },
    {
      "permCd": "BOARD_DELETE",
      "roleId": 10,
      "roleName": "board manager",
      "scopeType": "DEPT",
      "scopeId": "10"
    }
  ]
}
```

`AuthorizationService` should read scoped permissions from the current `AuthorizationUserDetails` or from the Redis-backed session object attached during JWT authentication. It should not query RBAC tables on every request unless the session cache is missing or explicitly refreshed.

## Permission Change Invalidation

Continue using `TB_AUTH_VERSION` and JWT `authVersion`.

When role assignments or role-permission mappings change for a user:

```text
1. Update RBAC tables.
2. Increment TB_AUTH_VERSION.AUTH_VER for affected employees.
3. Delete affected Redis sessions.
4. Existing tokens fail because their session id no longer resolves.
5. The user signs in again or goes through a reauthentication flow.
```

For changes that affect a role globally, such as adding `BOARD_DELETE` to a role, all employees with active assignments for that role are affected and their sessions should be invalidated.

## Examples

Department board manager:

```text
Request: BOARD_DELETE, boardId=55
Resource: TB_BOARD.DEPT_CD=10
User permission: BOARD_DELETE, scope=DEPT, scopeId=10
Result: allow
```

Wrong department:

```text
Request: BOARD_DELETE, boardId=56
Resource: TB_BOARD.DEPT_CD=11
User permission: BOARD_DELETE, scope=DEPT, scopeId=10
Result: deny
```

Project task update:

```text
Request: TASK_UPDATE, taskId=800
Resource: TB_TASK.PROJ_ID=300
User permission: TASK_UPDATE, scope=PROJECT, scopeId=300
Result: allow
```

Self profile update:

```text
Request: EMPLOYEE_UPDATE, targetEmpId=100
Resource: ownerEmpId=100
User permission: EMPLOYEE_UPDATE, scope=SELF, scopeId=*
Result: allow
```

## Error Handling

Authorization failures should return the existing 403 response format through `CustomAccessDeniedHandler` or a domain-level exception mapped to the same API response shape.

Recommended error categories:

```text
UNAUTHORIZED: no valid authentication
ACCESS_DENIED: authenticated but missing permission or scope
INVALID_SCOPE: admin attempted to create an invalid role assignment
AUTH_VERSION_MISMATCH: token/session auth version is stale
```

## Testing Strategy

Unit tests:

- `AuthorizationService` allows matching `GLOBAL`, `DEPT`, `PROJECT`, and `SELF` scoped permissions.
- `AuthorizationService` denies matching permission with wrong scope.
- `AuthorizationService` denies matching scope with missing permission.
- `RoleAssignmentService` rejects invalid scope ids.

Integration tests:

- Login/session creation loads scoped permissions into Redis.
- JWT authentication rebuilds the authenticated principal with scoped permissions.
- Board service allows department manager for the matching department.
- Board service denies department manager for a different department.
- Permission changes invalidate active Redis sessions.

## Migration Notes

1. Rename `TB_ROLE_MEMBER_MAPPING` to `TB_ROLE_ASSIGNMENT`.
2. Add `ROLE_ASSIGN_ID`, `SCOPE_TYPE_CD`, `SCOPE_ID`, `ENABLED`, and audit columns.
3. Backfill existing rows as `GLOBAL` assignments with `SCOPE_ID='*'`.
4. Add primary key, foreign keys, check constraints, and unique constraint.
5. Update RBAC queries to read only `TB_ROLE_ASSIGNMENT`.
6. Extend session creation to load `ScopedPermission` rows.
7. Replace global `.anyRequest().hasRole(...)` authorization with `.authenticated()`.
8. Add explicit service-layer authorization checks per resource operation.

## Open Implementation Choices

The design intentionally leaves these as implementation details:

- Whether `ScopedPermission` is stored directly inside `AuthorizationUserDetails` or resolved from Redis inside `AuthorizationService`.
- Whether `ROLE_ASSIGN` must always be `GLOBAL` or can be delegated by `DEPT`.
- Whether project membership grants implicit read access or is represented as explicit `PROJECT_READ` assignments.

These choices should be resolved during implementation planning for the first protected resource, likely board management.
