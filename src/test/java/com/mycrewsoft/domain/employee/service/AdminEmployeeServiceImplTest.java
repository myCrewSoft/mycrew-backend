package com.mycrewsoft.domain.employee.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.employee.dto.request.EmployeeRegisterRequestDTO;
import com.mycrewsoft.domain.employee.mapper.AdminEmployeeMapper;
import com.mycrewsoft.domain.employee.vo.EmployeeVO;
import com.mycrewsoft.domain.empstat.code.EmpStatCode;
import com.mycrewsoft.domain.roleassignment.service.RoleAssignmentService;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.PermissionCode;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.rbac.RbacAuthorizationChangeService;

@ExtendWith(MockitoExtension.class)
class AdminEmployeeServiceImplTest {

    @Mock
    private DtoMapper dtoMapper;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private AdminEmployeeMapper employeeMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Mock
    private RbacAuthorizationChangeService rbacAuthorizationChangeService;

    @InjectMocks
    private AdminEmployeeServiceImpl adminEmployeeService;

    @Test
    void registerEmployeeCreatesEmployeeThenAssignsDefaultRoleAndRefreshesAuthVersion() {
        EmployeeRegisterRequestDTO request = registerRequest();
        EmployeeVO employee = new EmployeeVO();
        employee.setEmpId(request.getEmpId());

        when(employeeMapper.selectEmployeeById(request.getEmpId())).thenReturn(null);
        when(dtoMapper.toDto(request, EmployeeVO.class)).thenReturn(employee);
        when(passwordEncoder.encode(String.valueOf(request.getEmpId()))).thenReturn("{bcrypt}encoded");

        adminEmployeeService.registerEmployee(request);

        ArgumentCaptor<EmployeeVO> employeeCaptor = ArgumentCaptor.forClass(EmployeeVO.class);
        verify(employeeMapper).insertEmployee(employeeCaptor.capture());

        EmployeeVO savedEmployee = employeeCaptor.getValue();
        assertThat(savedEmployee.getEmpId()).isEqualTo(request.getEmpId());
        assertThat(savedEmployee.getPswd()).isEqualTo("{bcrypt}encoded");
        assertThat(savedEmployee.getEnabled()).isEqualTo("Y");
        assertThat(savedEmployee.getEmpStatCd()).isEqualTo(EmpStatCode.EMP_INITIAL.getCode());
        assertThat(savedEmployee.getFrstRegDt()).isNotNull();

        verify(authorizationService).assertCurrentUserPermission(
                org.mockito.ArgumentMatchers.eq(PermissionCode.EMPLOYEE_CREATE),
                any(ResourceContext.class));

        InOrder order = inOrder(employeeMapper, roleAssignmentService, rbacAuthorizationChangeService);
        order.verify(employeeMapper).insertEmployee(savedEmployee);
        order.verify(roleAssignmentService).assignDefaultEmployeeRole(request.getEmpId());
        order.verify(rbacAuthorizationChangeService).refreshEmployeePermissions(request.getEmpId());
    }

    @Test
    void registerEmployeeRejectsDuplicateEmpId() {
        EmployeeRegisterRequestDTO request = registerRequest();
        when(employeeMapper.selectEmployeeById(request.getEmpId())).thenReturn(new EmployeeVO());

        assertThatThrownBy(() -> adminEmployeeService.registerEmployee(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMPLOYEEID);

        verify(employeeMapper, never()).insertEmployee(any(EmployeeVO.class));
        verify(roleAssignmentService, never()).assignDefaultEmployeeRole(any());
        verify(rbacAuthorizationChangeService, never()).refreshEmployeePermissions(any());
    }

    private EmployeeRegisterRequestDTO registerRequest() {
        EmployeeRegisterRequestDTO request = new EmployeeRegisterRequestDTO();
        request.setEmpId(20260001L);
        request.setEmpNm("Hong Gil Dong");
        request.setRrno("9001011234567");
        request.setGenderCd("M");
        request.setMblTelno("010-1234-5678");
        request.setZip("06234");
        request.setAddr("Seoul");
        request.setEntcoYmd(LocalDate.of(2026, 5, 27));
        return request;
    }
}
