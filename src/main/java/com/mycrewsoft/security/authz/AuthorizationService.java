package com.mycrewsoft.security.authz;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.util.SecurityUtil;

@Service
public class AuthorizationService {

    private final AuthorizationRelationshipResolver relationshipResolver;

    @Autowired
    public AuthorizationService(AuthorizationRelationshipResolver relationshipResolver) {
        this.relationshipResolver = relationshipResolver == null
                ? AuthorizationRelationshipResolver.noop()
                : relationshipResolver;
    }

    public AuthorizationService() {
        this(AuthorizationRelationshipResolver.noop());
    }

    public boolean canAccess(PermissionCode permissionCode, ResourceContext resource) {
        return canAccess(
                SecurityUtil.getCurrentEmpId(),
                SecurityUtil.getCurrentScopedPermissions(),
                permissionCode,
                resource);
    }

    public boolean canAccess(
            Long empId,
            Collection<ScopedPermission> scopedPermissions,
            PermissionCode permissionCode,
            ResourceContext resource) {
        if (empId == null || scopedPermissions == null || permissionCode == null || resource == null) {
            return false;
        }

        List<ScopedPermission> matchingPermissions = scopedPermissions.stream()
                .filter(permission -> permission != null && permissionCode.getCode().equals(permission.getPermCd()))
                .toList();

        if (matchingPermissions.isEmpty()) {
            return false;
        }

        if (isFeatureGate(resource)) {
            return matchingPermissions.stream().anyMatch(permission -> permission.getScopeType() != null);
        }

        return matchingPermissions.stream()
                .anyMatch(permission -> permissionAllows(empId, permission, permissionCode, resource));
    }

    public void assertCurrentUserPermission(PermissionCode permissionCode, ResourceContext resource) {
        if (!canAccess(permissionCode, resource)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void assertCanAccess(
            Long empId,
            Collection<ScopedPermission> scopedPermissions,
            PermissionCode permissionCode,
            ResourceContext resource) {
        if (!canAccess(empId, scopedPermissions, permissionCode, resource)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public PermissionScopeSet getCurrentPermissionScopes(PermissionCode permissionCode) {
        return getPermissionScopes(SecurityUtil.getCurrentScopedPermissions(), permissionCode);
    }

    public PermissionScopeSet getPermissionScopes(
            Collection<ScopedPermission> scopedPermissions,
            PermissionCode permissionCode) {
        return PermissionScopeSet.from(scopedPermissions, permissionCode);
    }

    public boolean hasGlobalScope(PermissionCode permissionCode) {
        return getCurrentPermissionScopes(permissionCode).hasGlobal();
    }

    private boolean permissionAllows(
            Long empId,
            ScopedPermission permission,
            PermissionCode permissionCode,
            ResourceContext resource) {
        return scopeMatchesExplicitly(empId, permission, permissionCode, resource)
                || relationshipPolicyMatches(empId, permission, permissionCode, resource);
    }

    private boolean scopeMatchesExplicitly(
            Long empId,
            ScopedPermission permission,
            PermissionCode permissionCode,
            ResourceContext resource) {
        if (permission.getScopeType() == null) {
            return false;
        }

        return switch (permission.getScopeType()) {
            case GLOBAL -> true;
            case DEPT -> same(permission.getScopeId(), resource.getDeptCd());
            case PROJECT -> same(permission.getScopeId(), effectiveProjectId(resource));
            case TASK -> same(permission.getScopeId(), resource.getTaskId());
            case SELF -> selfScopeMatches(empId, permissionCode, resource);
        };
    }

    private boolean relationshipPolicyMatches(
            Long empId,
            ScopedPermission permission,
            PermissionCode permissionCode,
            ResourceContext resource) {
        if (permission.getScopeType() != ScopeType.SELF) {
            return false;
        }

        String projectId = effectiveProjectId(resource);

        return switch (permissionCode) {
            case BOARD_POST_READ -> relationshipResolver.isSameDepartment(empId, resource.getDeptCd())
                    || relationshipResolver.isProjectMember(empId, projectId);
            case BOARD_POST_CREATE -> canCreateBoardPostByRelationship(empId, resource, projectId);
            case DEPT_READ -> relationshipResolver.isSameDepartment(empId, resource.getDeptCd());
            case PROJECT_READ,
                    PROJECT_DRIVE_READ,
                    PROJECT_DRIVE_UPLOAD,
                    PROJECT_DRIVE_UPDATE,
                    PROJECT_DRIVE_DELETE,
                    PROJECT_DRIVE_MANAGE -> relationshipResolver.isProjectMember(empId, projectId);
            case PROJECT_UPDATE,
                    PROJECT_DELETE,
                    PROJECT_MEMBER_MANAGE -> relationshipResolver.isProjectLeader(empId, projectId);
            case SCHEDULE_READ -> Objects.equals(empId, resource.getOwnerEmpId())
                    || relationshipResolver.isSameDepartment(empId, resource.getDeptCd())
                    || relationshipResolver.isProjectMember(empId, projectId);
            default -> false;
        };
    }

    private boolean canCreateBoardPostByRelationship(Long empId, ResourceContext resource, String projectId) {
        if (StringUtils.hasText(resource.getDeptCd())) {
            return relationshipResolver.isSameDepartment(empId, resource.getDeptCd());
        }

        if (StringUtils.hasText(projectId)) {
            return relationshipResolver.isProjectMember(empId, projectId);
        }

        return false;
    }

    private boolean selfScopeMatches(Long empId, PermissionCode permissionCode, ResourceContext resource) {
        if (permissionCode == PermissionCode.BOARD_POST_CREATE && hasSharedBoardTarget(resource)) {
            return false;
        }

        return Objects.equals(empId, resource.getOwnerEmpId());
    }

    private boolean hasSharedBoardTarget(ResourceContext resource) {
        return StringUtils.hasText(resource.getDeptCd()) || StringUtils.hasText(effectiveProjectId(resource));
    }

    private boolean isFeatureGate(ResourceContext resource) {
        return !StringUtils.hasText(resource.getResourceId())
                && !StringUtils.hasText(resource.getDeptCd())
                && !StringUtils.hasText(resource.getProjId())
                && !StringUtils.hasText(resource.getTaskId())
                && resource.getOwnerEmpId() == null
                && resource.getManagerEmpId() == null
                && (resource.getMemberEmpIds() == null || resource.getMemberEmpIds().isEmpty());
    }

    private String effectiveProjectId(ResourceContext resource) {
        if (StringUtils.hasText(resource.getProjId())) {
            return resource.getProjId();
        }

        if (resource.getResourceType() == ResourceType.PROJECT && StringUtils.hasText(resource.getResourceId())) {
            return resource.getResourceId();
        }

        return null;
    }

    private boolean same(String assignmentScopeId, String resourceScopeId) {
        return StringUtils.hasText(assignmentScopeId)
                && StringUtils.hasText(resourceScopeId)
                && assignmentScopeId.trim().equals(resourceScopeId.trim());
    }
}
