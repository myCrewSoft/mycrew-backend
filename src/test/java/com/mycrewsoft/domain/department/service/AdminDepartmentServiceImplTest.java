package com.mycrewsoft.domain.department.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.department.dto.request.DepartmentCreateRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentDeleteRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentMemberAssignRequestDTO;
import com.mycrewsoft.domain.department.dto.response.AdminDepartmentResponseDTO;
import com.mycrewsoft.domain.department.dto.response.DepartmentMemberMutationResponseDTO;
import com.mycrewsoft.domain.department.mapper.AdminDepartmentMapper;
import com.mycrewsoft.domain.department.vo.DepartmentVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.rbac.RbacAuthorizationChangeService;

@ExtendWith(MockitoExtension.class)
class AdminDepartmentServiceImplTest {

    @Mock
    private AdminDepartmentMapper departmentMapper;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private RbacAuthorizationChangeService rbacAuthorizationChangeService;

    @Test
    void getDepartmentListChecksAdminDeptReadPermissionAndReturnsDepartments() {
        AdminDepartmentResponseDTO department = department("DEPT_001");
        when(departmentMapper.selectDepartments()).thenReturn(List.of(department));

        AdminDepartmentServiceImpl service = service();

        List<AdminDepartmentResponseDTO> departments = service.getDepartmentList();

        assertThat(departments).containsExactly(department);
        verify(authorizationService).assertCurrentUserPermission(
                eq(PermissionCode.ADMIN_DEPT_READ),
                any(ResourceContext.class));
    }

    @Test
    void createDepartmentRejectsDuplicateDepartmentCode() {
        DepartmentCreateRequestDTO request = new DepartmentCreateRequestDTO();
        request.setDeptCd("DEPT_001");
        request.setDeptNm("개발팀");
        when(departmentMapper.countActiveDepartmentByCode("DEPT_001")).thenReturn(1);

        AdminDepartmentServiceImpl service = service();

        assertThatThrownBy(() -> service.createDepartment(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_DEPARTMENT_CODE);

        verify(departmentMapper, never()).insertDepartment(any(DepartmentVO.class));
    }

    @Test
    void deleteDepartmentWithAssignedEmployeesRequiresReplacementDepartment() {
        when(departmentMapper.selectDepartmentByCode("DEPT_001")).thenReturn(department("DEPT_001"));
        when(departmentMapper.selectEnabledEmpIdsByDeptCd("DEPT_001")).thenReturn(List.of(1001L));

        AdminDepartmentServiceImpl service = service();

        assertThatThrownBy(() -> service.deleteDepartment("DEPT_001", new DepartmentDeleteRequestDTO()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        verify(departmentMapper, never()).disableDepartment(eq("DEPT_001"), any());
    }

    @Test
    void assignDepartmentMembersUpdatesEmployeesAndRefreshesAuthVersions() {
        DepartmentMemberAssignRequestDTO request = new DepartmentMemberAssignRequestDTO();
        request.setEmpIds(List.of(1001L, 1002L));
        when(departmentMapper.selectDepartmentByCode("DEPT_001")).thenReturn(department("DEPT_001"));
        when(departmentMapper.countEnabledEmployeesByIds(request.getEmpIds())).thenReturn(2);

        AdminDepartmentServiceImpl service = service();

        DepartmentMemberMutationResponseDTO result = service.assignDepartmentMembers("DEPT_001", request);

        assertThat(result.getTargetDeptCd()).isEqualTo("DEPT_001");
        assertThat(result.getAffectedEmployeeCount()).isEqualTo(2);
        verify(departmentMapper).updateDepartmentForEmployees("DEPT_001", request.getEmpIds());
        verify(rbacAuthorizationChangeService).refreshEmployeesPermissions(request.getEmpIds());
        verify(authorizationService).assertCurrentUserPermission(
                eq(PermissionCode.ADMIN_DEPT_MEMBER_MANAGE),
                any(ResourceContext.class));
    }

    private AdminDepartmentServiceImpl service() {
        return new AdminDepartmentServiceImpl(
                departmentMapper,
                authorizationService,
                rbacAuthorizationChangeService);
    }

    private AdminDepartmentResponseDTO department(String deptCd) {
        AdminDepartmentResponseDTO department = new AdminDepartmentResponseDTO();
        department.setDeptCd(deptCd);
        department.setDeptNm("개발팀");
        department.setUseYn("Y");
        department.setMemberCount(0);
        return department;
    }
}
