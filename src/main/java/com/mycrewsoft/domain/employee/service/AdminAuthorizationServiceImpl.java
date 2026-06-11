package com.mycrewsoft.domain.employee.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mycrewsoft.common.constant.Constants;
import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.employee.dto.request.PermissionStatusUpdateRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleAssignRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleCreateRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleDeleteRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleRevokeRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleUpdateRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.PermissionResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleDetailResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleListResponseDTO;
import com.mycrewsoft.domain.employee.event.PermissionChangedEvent;
import com.mycrewsoft.domain.employee.mapper.AdminAuthorizationMapper;
import com.mycrewsoft.domain.role.vo.RoleVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.authz.ScopeType;
import com.mycrewsoft.security.rbac.RbacAuthorizationChangeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAuthorizationServiceImpl implements AdminAuthorizationService {

    private final AuthorizationService authorizationService;
    private final AdminAuthorizationMapper adminAuthorizationMapper;
    private final RbacAuthorizationChangeService rbacAuthorizationChangeService;
    private final ApplicationEventPublisher eventPublisher;
    
    
    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponseDTO> getPermissions() {
        assertRoleManagePermission();
        List<PermissionResponseDTO> permissions = adminAuthorizationMapper.selectPermissions();
        return permissions == null ? List.of() : permissions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleListResponseDTO> getRoles() {
        assertRoleManagePermission();
        List<RoleListResponseDTO> roles = adminAuthorizationMapper.selectRoles();
        return roles == null ? List.of() : roles;
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDetailResponseDTO getRole(Long roleId) {
        assertRoleManagePermission();
        return loadRoleDetail(roleId);
    }

    @Override
    @Transactional
    public RoleDetailResponseDTO createRole(RoleCreateRequestDTO request) {
        assertRoleManagePermission();
        validateCreateRequest(request);
        List<Long> permissionIds = normalizePermissionIds(request.getPermissionIds());

        if (adminAuthorizationMapper.selectRoleIdByRoleCode(request.getRoleCode()) != null) {
            throw new CustomException(ErrorCode.DUPLICATE_ROLE_CODE);
        }

        validatePermissionIds(permissionIds);

        RoleVO role = new RoleVO();
        role.setRoleCd(request.getRoleCode().trim());
        role.setRoleNm(request.getRoleName().trim());
        role.setRoleExpln(request.getDescription());
        role.setFrstRegDt(LocalDateTime.now());
        role.setLastMdfcnDt(LocalDateTime.now());

        adminAuthorizationMapper.insertRole(role);

        Long roleId = adminAuthorizationMapper.selectRoleIdByRoleCode(role.getRoleCd());
        if (roleId == null) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        adminAuthorizationMapper.insertPermissionMappings(roleId, permissionIds);
        return loadRoleDetail(roleId);
    }

    @Override
    @Transactional
    public RoleDetailResponseDTO updateRole(Long roleId, RoleUpdateRequestDTO request) {
        assertRoleManagePermission();
        RoleDetailResponseDTO role = loadRoleBase(roleId);
        validateUpdateRequest(request);

        List<Long> permissionIds = normalizePermissionIds(request.getPermissionIds());
        validatePermissionIds(permissionIds);

        adminAuthorizationMapper.updateRole(
                role.getRoleId(),
                request.getRoleName().trim(),
                request.getDescription(),
                null);
        adminAuthorizationMapper.deletePermissionMappingsByRoleId(role.getRoleId());
        adminAuthorizationMapper.insertPermissionMappings(role.getRoleId(), permissionIds);
        rbacAuthorizationChangeService.refreshRolePermissions(role.getRoleId());

        return loadRoleDetail(role.getRoleId());
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId, RoleDeleteRequestDTO request) {
        assertRoleManagePermission();
        RoleDetailResponseDTO role = loadRoleBase(roleId);
        if (isProtectedSystemRole(role.getRoleCode())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<Long> affectedEmpIds = distinctNonNull(adminAuthorizationMapper.selectEnabledEmpIdsByRoleId(roleId));
        Long replacementRoleId = request == null ? null : request.getReplacementRoleId();

        if (!affectedEmpIds.isEmpty()) {
            validateReplacementRole(roleId, replacementRoleId);
            adminAuthorizationMapper.insertReplacementRoleAssignments(roleId, replacementRoleId);
        }

        adminAuthorizationMapper.deleteRoleAssignmentsByRoleId(roleId);
        adminAuthorizationMapper.deletePermissionMappingsByRoleId(roleId);
        adminAuthorizationMapper.deleteRole(roleId);

        if (!affectedEmpIds.isEmpty()) {
            rbacAuthorizationChangeService.refreshEmployeesPermissions(affectedEmpIds);
        }
    }

    @Override
    @Transactional
    public RoleDetailResponseDTO assignRole(Long roleId, RoleAssignRequestDTO request) {
        assertRoleManagePermission();
        RoleDetailResponseDTO role = loadRoleBase(roleId);
        List<Long> empIds = normalizeEmpIds(request == null ? null : request.getEmpIds());
        String scopeTypeCd = normalizeScopeType(request == null ? null : request.getScopeTypeCd());
        String scopeId = normalizeAssignmentScopeId(scopeTypeCd, request == null ? null : request.getScopeId());

        validateEnabledEmployees(empIds);

        adminAuthorizationMapper.insertRoleAssignments(role.getRoleId(), empIds, scopeTypeCd, scopeId);
        rbacAuthorizationChangeService.refreshEmployeesPermissions(empIds);

        // 권한 변경 알림
        eventPublisher.publishEvent(
        	new PermissionChangedEvent(request.getEmpIds())
        );
        
        return loadRoleDetail(role.getRoleId());
    }

    @Override
    @Transactional
    public RoleDetailResponseDTO revokeRole(Long roleId, RoleRevokeRequestDTO request) {
        assertRoleManagePermission();
        RoleDetailResponseDTO role = loadRoleBase(roleId);
        List<Long> empIds = normalizeEmpIds(request == null ? null : request.getEmpIds());
        String scopeTypeCd = normalizeOptionalScopeType(request == null ? null : request.getScopeTypeCd());
        String scopeId = normalizeOptionalScopeId(scopeTypeCd, request == null ? null : request.getScopeId());

        adminAuthorizationMapper.deleteRoleAssignments(role.getRoleId(), empIds, scopeTypeCd, scopeId);
        rbacAuthorizationChangeService.refreshEmployeesPermissions(empIds);

        // 권한 변경 알림
        eventPublisher.publishEvent(
        	new PermissionChangedEvent(request.getEmpIds())
        );
        
        return loadRoleDetail(role.getRoleId());
    }

    @Override
    @Transactional
    public PermissionResponseDTO updatePermissionStatus(Long permissionId, PermissionStatusUpdateRequestDTO request) {
        assertRoleManagePermission();
        String enabled = normalizeEnabled(request == null ? null : request.getEnabled());
        PermissionResponseDTO permission = loadPermission(permissionId);

        adminAuthorizationMapper.updatePermissionEnabled(permission.getPermissionId(), enabled);
        nullToEmpty(adminAuthorizationMapper.selectRoleIdsByPermissionId(permission.getPermissionId()))
                .forEach(rbacAuthorizationChangeService::refreshRolePermissions);

        permission.setEnabled(enabled);
        return permission;
    }

    private void assertRoleManagePermission() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.ADMIN)
                .build();

        authorizationService.assertCurrentUserPermission(PermissionCode.ADMIN_ROLE_MANAGE, resource);
    }

    private RoleDetailResponseDTO loadRoleDetail(Long roleId) {
        RoleDetailResponseDTO role = loadRoleBase(roleId);
        role.setPermissions(nullToEmpty(adminAuthorizationMapper.selectPermissionsByRoleId(roleId)));
        role.setEmployees(nullToEmpty(adminAuthorizationMapper.selectEmployeesByRoleId(roleId)));
        return role;
    }

    private RoleDetailResponseDTO loadRoleBase(Long roleId) {
        if (roleId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        RoleDetailResponseDTO role = adminAuthorizationMapper.selectRoleDetail(roleId);
        if (role == null) {
            throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    private PermissionResponseDTO loadPermission(Long permissionId) {
        if (permissionId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        PermissionResponseDTO permission = adminAuthorizationMapper.selectPermissionById(permissionId);
        if (permission == null) {
            throw new CustomException(ErrorCode.PERMISSION_NOT_FOUND);
        }

        return permission;
    }

    private void validateCreateRequest(RoleCreateRequestDTO request) {
        if (request == null
                || !StringUtils.hasText(request.getRoleCode())
                || !StringUtils.hasText(request.getRoleName())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateUpdateRequest(RoleUpdateRequestDTO request) {
        if (request == null || !StringUtils.hasText(request.getRoleName())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private List<Long> normalizePermissionIds(List<Long> permissionIds) {
        List<Long> normalized = distinctNonNull(permissionIds);
        if (normalized.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return normalized;
    }

    private List<Long> normalizeEmpIds(List<Long> empIds) {
        List<Long> normalized = distinctNonNull(empIds);
        if (normalized.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return normalized;
    }

    private void validatePermissionIds(List<Long> permissionIds) {
        int enabledPermissionCount = adminAuthorizationMapper.countEnabledPermissionsByIds(permissionIds);
        if (enabledPermissionCount != permissionIds.size()) {
            throw new CustomException(ErrorCode.PERMISSION_NOT_FOUND);
        }
    }

    private void validateEnabledEmployees(List<Long> empIds) {
        int enabledEmployeeCount = adminAuthorizationMapper.countEnabledEmployeesByIds(empIds);
        if (enabledEmployeeCount != empIds.size()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private String normalizeScopeType(String scopeTypeCd) {
        if (!StringUtils.hasText(scopeTypeCd)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        try {
            return ScopeType.valueOf(scopeTypeCd.trim()).name();
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String normalizeOptionalScopeType(String scopeTypeCd) {
        if (!StringUtils.hasText(scopeTypeCd)) {
            return null;
        }

        return normalizeScopeType(scopeTypeCd);
    }

    private String normalizeAssignmentScopeId(String scopeTypeCd, String scopeId) {
        if (ScopeType.GLOBAL.name().equals(scopeTypeCd)) {
            return StringUtils.hasText(scopeId) ? scopeId.trim() : "*";
        }

        if (ScopeType.SELF.name().equals(scopeTypeCd)) {
            return null;
        }

        if (!StringUtils.hasText(scopeId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return scopeId.trim();
    }

    private String normalizeOptionalScopeId(String scopeTypeCd, String scopeId) {
        if (scopeTypeCd == null) {
            return null;
        }

        if (ScopeType.SELF.name().equals(scopeTypeCd)) {
            return null;
        }

        if (ScopeType.GLOBAL.name().equals(scopeTypeCd)) {
            return StringUtils.hasText(scopeId) ? scopeId.trim() : "*";
        }

        if (!StringUtils.hasText(scopeId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return scopeId.trim();
    }

    private String normalizeEnabled(String enabled) {
        if (!StringUtils.hasText(enabled)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String normalized = enabled.trim().toUpperCase();
        if (!"Y".equals(normalized) && !"N".equals(normalized)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return normalized;
    }

    private void validateReplacementRole(Long roleId, Long replacementRoleId) {
        if (replacementRoleId == null || Objects.equals(roleId, replacementRoleId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        loadRoleBase(replacementRoleId);
    }

    private boolean isProtectedSystemRole(String roleCode) {
        return Constants.SUPER_ADMIN.equals(roleCode)
                || Constants.ROLE_EMPLOYEE_SELF.equals(roleCode);
    }

    private <T> List<T> nullToEmpty(List<T> values) {
        return values == null ? List.of() : values;
    }

    private List<Long> distinctNonNull(List<Long> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
