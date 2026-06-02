package com.mycrewsoft.security.authz;

import java.util.Collection;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.util.SecurityUtil;

/**
 * Central authorization service for scoped RBAC.
 *
 * Responsibilities:
 * - Check whether a user can access a resource with a PermissionCode.
 * - Expose the current user's scopes for list-query filtering.
 * - Keep scope matching rules in one place.
 */
@Service
public class AuthorizationService {

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

        return scopedPermissions.stream()
                .filter(permission -> permissionCode.getCode().equals(permission.getPermCd()))
                .anyMatch(permission -> scopeMatches(empId, permission, resource));
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

    private boolean scopeMatches(Long empId, ScopedPermission permission, ResourceContext resource) {
        if (permission.getScopeType() == null) {
            return false;
        }

        return switch (permission.getScopeType()) {
            case GLOBAL -> true;
            case DEPT -> same(permission.getScopeId(), resource.getDeptCd());
            case PROJECT -> same(permission.getScopeId(), resource.getProjId());
            case TASK -> same(permission.getScopeId(), resource.getTaskId());
            case SELF -> Objects.equals(empId, resource.getOwnerEmpId());
        };
    }

    private boolean same(String assignmentScopeId, String resourceScopeId) {
        return assignmentScopeId != null
                && resourceScopeId != null
                && assignmentScopeId.trim().equals(resourceScopeId.trim());
    }
}
