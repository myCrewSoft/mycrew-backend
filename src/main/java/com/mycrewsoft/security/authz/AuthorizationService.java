package com.mycrewsoft.security.authz;

import java.util.Collection;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.util.SecurityUtil;

/**
 * 리소스 범위 기반 인가를 수행하는 서비스.
 *
 * 역할:
 * - 현재 사용자의 scopedPermissions를 기준으로 특정 리소스에 대한 권한을 검사한다.
 * - GLOBAL, DEPT, PROJECT, SELF 같은 scopeType별 매칭 규칙을 적용한다.
 *
 * 사용 예:
 * - 게시글 수정 가능 여부 확인
 * - 프로젝트 문서 접근 가능 여부 확인
 * - 부서 단위 리소스 관리 권한 확인
 */
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

    public boolean hasPermission(
            Long empId,
            Collection<ScopedPermission> scopedPermissions,
            PermissionCode permissionCode,
            ResourceContext resource) {
        return permissionCode != null
                && hasPermission(empId, scopedPermissions, permissionCode.getCode(), resource);
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

    public void assertPermission(
            Long empId,
            Collection<ScopedPermission> scopedPermissions,
            PermissionCode permissionCode,
            ResourceContext resource) {
        if (!hasPermission(empId, scopedPermissions, permissionCode, resource)) {
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

    public boolean hasCurrentUserPermission(PermissionCode permissionCode, ResourceContext resource) {
        return permissionCode != null
                && hasCurrentUserPermission(permissionCode.getCode(), resource);
    }

    public void assertCurrentUserPermission(String permCd, ResourceContext resource) {
        if (!hasCurrentUserPermission(permCd, resource)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void assertCurrentUserPermission(PermissionCode permissionCode, ResourceContext resource) {
        if (!hasCurrentUserPermission(permissionCode, resource)) {
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
