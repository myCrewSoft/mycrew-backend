package com.mycrewsoft.domain.roleassignment.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.common.constant.Constants;
import com.mycrewsoft.domain.roleassignment.mapper.RoleAssignmentMapper;
import com.mycrewsoft.domain.roleassignment.vo.RoleAssignmentVO;
import com.mycrewsoft.security.authz.ScopeType;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentServiceTest {

    @Mock
    private RoleAssignmentMapper roleAssignmentMapper;

    @InjectMocks
    private RoleAssignmentServiceImpl roleAssignmentService;

    @Test
    void assignDefaultEmployeeRoleAssignsSelfScopedEmployeeRole() {
        Long empId = 20260001L;
        Long roleId = 10L;
        when(roleAssignmentMapper.selectRoleIdByRoleCode(Constants.ROLE_EMPLOYEE_SELF))
                .thenReturn(roleId);

        roleAssignmentService.assignDefaultEmployeeRole(empId);

        ArgumentCaptor<RoleAssignmentVO> captor = ArgumentCaptor.forClass(RoleAssignmentVO.class);
        verify(roleAssignmentMapper).insertRoleAssignmentIfAbsent(captor.capture());

        RoleAssignmentVO assignment = captor.getValue();
        assertThat(assignment.getRoleId()).isEqualTo(roleId);
        assertThat(assignment.getEmpId()).isEqualTo(empId);
        assertThat(assignment.getScopeTypeCd()).isEqualTo(ScopeType.SELF.name());
        assertThat(assignment.getScopeId()).isEqualTo(String.valueOf(empId));
        assertThat(assignment.getEnabled()).isEqualTo("Y");
    }
}
