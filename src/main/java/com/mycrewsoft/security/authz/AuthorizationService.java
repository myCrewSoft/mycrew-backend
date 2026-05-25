package com.mycrewsoft.security.authz;

import java.util.Collection;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.util.SecurityUtil;

@Service
public class AuthorizationService {

    public boolean hasPermission(
            Long empId,
            Collection<ScopedPermission> scopedPermissions,
            String permCd,
            ResourceContext resource) {
        if (empId == null || scopedPermissions == null || permCd == null || resource == null) {
            return false;
        }

        return scopedPermissions.stream()
                .filter(permission -> permCd.equals(permission.getPermCd()))
                .anyMatch(permission -> scopeMatches(empId, permission, resource));
    }

    public void assertPermission(
            Long empId,
            Collection<ScopedPermission> scopedPermissions,
            String permCd,
            ResourceContext resource) {
        if (!hasPermission(empId, scopedPermissions, permCd, resource)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public boolean hasCurrentUserPermission(String permCd, ResourceContext resource) {
        return hasPermission(
                SecurityUtil.getCurrentEmpId(),
                SecurityUtil.getCurrentScopedPermissions(),
                permCd,
                resource);
    }

    public void assertCurrentUserPermission(String permCd, ResourceContext resource) {
        if (!hasCurrentUserPermission(permCd, resource)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    private boolean scopeMatches(Long empId, ScopedPermission permission, ResourceContext resource) {
        if (permission.getScopeType() == null) {
            return false;
        }

        return switch (permission.getScopeType()) {
            case GLOBAL -> true;
            case DEPT -> same(permission.getScopeId(), resource.getDeptCd());
            case PROJECT -> same(permission.getScopeId(), resource.getProjId());
            case SELF -> Objects.equals(empId, resource.getOwnerEmpId());
        };
    }

    private boolean same(String assignmentScopeId, String resourceScopeId) {
        return assignmentScopeId != null
                && resourceScopeId != null
                && assignmentScopeId.trim().equals(resourceScopeId.trim());
    }
}
