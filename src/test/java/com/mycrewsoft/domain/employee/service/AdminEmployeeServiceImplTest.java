package com.mycrewsoft.domain.employee.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DtoMapper;
import com.mycrewsoft.domain.employee.dto.request.EmployeeRegisterRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.EmployeeSearchDTO;
import com.mycrewsoft.domain.employee.dto.request.EmployeeStatusUpdateRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeListDTO;
import com.mycrewsoft.domain.employee.mapper.AdminEmployeeMapper;
import com.mycrewsoft.domain.employee.vo.EmployeeVO;
import com.mycrewsoft.domain.empstat.code.EmpStatCode;
import com.mycrewsoft.domain.roleassignment.service.RoleAssignmentService;
import com.mycrewsoft.security.authz.AuthorizationService;
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

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(
                adminEmployeeService,
                "adminEmployeeMapper",
                employeeMapper);
    }

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
                org.mockito.ArgumentMatchers.eq(PermissionCode.ADMIN_EMPLOYEE_CREATE),
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

    @Test
    void getEmployeesChecksPermissionAndReturnsPagedEmployees() {
        EmployeeSearchDTO condition = new EmployeeSearchDTO();
        condition.setPage(1);
        condition.setSize(2);

        EmployeeListDTO firstEmployee = new EmployeeListDTO();
        firstEmployee.setEmpId(20260003L);
        firstEmployee.setEmpNm("Kim");
        EmployeeListDTO secondEmployee = new EmployeeListDTO();
        secondEmployee.setEmpId(20260002L);
        secondEmployee.setEmpNm("Lee");
        List<EmployeeListDTO> employees = List.of(firstEmployee, secondEmployee);

        when(employeeMapper.countEmployees(condition)).thenReturn(5L);
        when(employeeMapper.selectEmployees(condition, 2, 2)).thenReturn(employees);

        Page<EmployeeListDTO> page = adminEmployeeService.getEmployees(condition);

        assertThat(page.getContent()).containsExactlyElementsOf(employees);
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(5L);
        assertThat(page.getTotalPages()).isEqualTo(3);

        verify(authorizationService).assertCurrentUserPermission(
                org.mockito.ArgumentMatchers.eq(PermissionCode.ADMIN_EMPLOYEE_READ),
                any(ResourceContext.class));
        verify(employeeMapper).selectEmployees(condition, 2, 2);
    }

    @Test
    void getEmployeesNormalizesPageAndSize() {
        EmployeeSearchDTO condition = new EmployeeSearchDTO();
        condition.setPage(-1);
        condition.setSize(1000);

        when(employeeMapper.countEmployees(condition)).thenReturn(0L);
        when(employeeMapper.selectEmployees(condition, 0, 100)).thenReturn(List.of());

        Page<EmployeeListDTO> page = adminEmployeeService.getEmployees(condition);

        assertThat(page.getNumber()).isZero();
        assertThat(page.getSize()).isEqualTo(100);
        assertThat(page.getTotalElements()).isZero();
        verify(employeeMapper).selectEmployees(condition, 0, 100);
    }

    @Test
    void updateEmployeeStatusChecksPermissionAndUpdatesStatus() {
        Long empId = 20260001L;
        EmployeeStatusUpdateRequestDTO request = new EmployeeStatusUpdateRequestDTO();
        request.setEmpStatCd(EmpStatCode.EMP_INACTIVE.getCode());

        EmployeeVO employee = new EmployeeVO();
        employee.setEmpId(empId);
        when(employeeMapper.selectEmployeeById(empId)).thenReturn(employee);

        adminEmployeeService.updateEmployeeStatus(empId, request);

        verify(authorizationService).assertCurrentUserPermission(
                org.mockito.ArgumentMatchers.eq(PermissionCode.ADMIN_EMPLOYEE_UPDATE),
                any(ResourceContext.class));
        verify(employeeMapper).updateEmployeeStatus(empId, EmpStatCode.EMP_INACTIVE.getCode());
    }

    @Test
    void updateEmployeeStatusRejectsMissingEmployee() {
        Long empId = 20260001L;
        EmployeeStatusUpdateRequestDTO request = new EmployeeStatusUpdateRequestDTO();
        request.setEmpStatCd(EmpStatCode.EMP_INACTIVE.getCode());

        when(employeeMapper.selectEmployeeById(empId)).thenReturn(null);

        assertThatThrownBy(() -> adminEmployeeService.updateEmployeeStatus(empId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(employeeMapper, never()).updateEmployeeStatus(any(), any());
    }

    @Test
    void updateEmployeeStatusRejectsUnknownStatusCode() {
        Long empId = 20260001L;
        EmployeeStatusUpdateRequestDTO request = new EmployeeStatusUpdateRequestDTO();
        request.setEmpStatCd("UNKNOWN_STATUS");

        EmployeeVO employee = new EmployeeVO();
        employee.setEmpId(empId);
        when(employeeMapper.selectEmployeeById(empId)).thenReturn(employee);

        assertThatThrownBy(() -> adminEmployeeService.updateEmployeeStatus(empId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        verify(employeeMapper, never()).updateEmployeeStatus(any(), any());
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
