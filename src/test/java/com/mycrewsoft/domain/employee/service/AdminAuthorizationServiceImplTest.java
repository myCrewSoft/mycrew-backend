package com.mycrewsoft.domain.employee.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.mycrewsoft.common.constant.Constants;
import com.mycrewsoft.domain.employee.dto.request.RoleAssignRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleDetailResponseDTO;
import com.mycrewsoft.domain.employee.mapper.AdminAuthorizationMapper;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ScopeType;
import com.mycrewsoft.security.rbac.RbacAuthorizationChangeService;

@ExtendWith(MockitoExtension.class)
class AdminAuthorizationServiceImplTest {

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private AdminAuthorizationMapper adminAuthorizationMapper;

    @Mock
    private RbacAuthorizationChangeService rbacAuthorizationChangeService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AdminAuthorizationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminAuthorizationServiceImpl(
                authorizationService,
                adminAuthorizationMapper,
                rbacAuthorizationChangeService,
                eventPublisher);
    }

    @Test
    void assignEmployeeSelfRoleAlwaysUsesSelfScope() {
        Long roleId = 10L;
        List<Long> empIds = List.of(1111L, 2222L);
        when(adminAuthorizationMapper.selectRoleDetail(roleId))
                .thenReturn(role(roleId, Constants.ROLE_EMPLOYEE_SELF));
        when(adminAuthorizationMapper.countEnabledEmployeesByIds(empIds)).thenReturn(empIds.size());

        RoleAssignRequestDTO request = request(empIds, null, null);

        service.assignRole(roleId, request);

        verify(adminAuthorizationMapper).insertRoleAssignments(
                eq(roleId),
                eq(empIds),
                eq(ScopeType.SELF.name()),
                eq(null));
    }

    @Test
    void assignSuperAdminRoleAlwaysUsesGlobalScope() {
        Long roleId = 1L;
        List<Long> empIds = List.of(1111L);
        when(adminAuthorizationMapper.selectRoleDetail(roleId))
                .thenReturn(role(roleId, Constants.SUPER_ADMIN));
        when(adminAuthorizationMapper.countEnabledEmployeesByIds(empIds)).thenReturn(empIds.size());

        RoleAssignRequestDTO request = request(empIds, ScopeType.DEPT.name(), "DEPT_024");

        service.assignRole(roleId, request);

        verify(adminAuthorizationMapper).insertRoleAssignments(
                eq(roleId),
                eq(empIds),
                eq(ScopeType.GLOBAL.name()),
                eq("*"));
    }

    @Test
    void assignCustomRoleKeepsRequestedScope() {
        Long roleId = 20L;
        List<Long> empIds = List.of(1111L);
        when(adminAuthorizationMapper.selectRoleDetail(roleId))
                .thenReturn(role(roleId, "ROLE_BOARD_MANAGER"));
        when(adminAuthorizationMapper.countEnabledEmployeesByIds(empIds)).thenReturn(empIds.size());

        RoleAssignRequestDTO request = request(empIds, ScopeType.DEPT.name(), "DEPT_024");

        service.assignRole(roleId, request);

        verify(adminAuthorizationMapper).insertRoleAssignments(
                eq(roleId),
                eq(empIds),
                eq(ScopeType.DEPT.name()),
                eq("DEPT_024"));
    }

    private RoleAssignRequestDTO request(List<Long> empIds, String scopeTypeCd, String scopeId) {
        RoleAssignRequestDTO request = new RoleAssignRequestDTO();
        request.setEmpIds(empIds);
        request.setScopeTypeCd(scopeTypeCd);
        request.setScopeId(scopeId);
        return request;
    }

    private RoleDetailResponseDTO role(Long roleId, String roleCode) {
        RoleDetailResponseDTO role = new RoleDetailResponseDTO();
        role.setRoleId(roleId);
        role.setRoleCode(roleCode);
        return role;
    }
}
