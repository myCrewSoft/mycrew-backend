package com.mycrewsoft.domain.employee.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.request.PermissionStatusUpdateRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleAssignRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleDeleteRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.PermissionResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleDetailResponseDTO;
import com.mycrewsoft.domain.employee.service.AdminAuthorizationService;

class AdminAuthorizationControllerTest {

    @Test
    void getPermissionsReturnsServiceResponse() {
        AdminAuthorizationService service = Mockito.mock(AdminAuthorizationService.class);
        AdminAuthorizationController controller = new AdminAuthorizationController(service);
        PermissionResponseDTO permission = new PermissionResponseDTO();
        permission.setPermissionId(1L);
        when(service.getPermissions()).thenReturn(List.of(permission));

        ApiResponse<List<PermissionResponseDTO>> response = controller.getPermissions();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsExactly(permission);
    }

    @Test
    void deleteRoleDelegatesReplacementRequestToService() {
        AdminAuthorizationService service = Mockito.mock(AdminAuthorizationService.class);
        AdminAuthorizationController controller = new AdminAuthorizationController(service);
        RoleDeleteRequestDTO request = new RoleDeleteRequestDTO();
        request.setReplacementRoleId(20L);

        ApiResponse<String> response = controller.deleteRole(10L, request);

        assertThat(response.isSuccess()).isTrue();
        verify(service).deleteRole(10L, request);
    }

    @Test
    void assignRoleDelegatesRequestToService() {
        AdminAuthorizationService service = Mockito.mock(AdminAuthorizationService.class);
        AdminAuthorizationController controller = new AdminAuthorizationController(service);
        RoleAssignRequestDTO request = new RoleAssignRequestDTO();
        RoleDetailResponseDTO role = new RoleDetailResponseDTO();
        role.setRoleId(10L);
        when(service.assignRole(10L, request)).thenReturn(role);

        ApiResponse<RoleDetailResponseDTO> response = controller.assignRole(10L, request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(role);
        verify(service).assignRole(10L, request);
    }

    @Test
    void updatePermissionStatusDelegatesRequestToService() {
        AdminAuthorizationService service = Mockito.mock(AdminAuthorizationService.class);
        AdminAuthorizationController controller = new AdminAuthorizationController(service);
        PermissionStatusUpdateRequestDTO request = new PermissionStatusUpdateRequestDTO();
        request.setEnabled("N");
        PermissionResponseDTO permission = new PermissionResponseDTO();
        permission.setPermissionId(1L);
        when(service.updatePermissionStatus(1L, request)).thenReturn(permission);

        ApiResponse<PermissionResponseDTO> response = controller.updatePermissionStatus(1L, request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(permission);
        verify(service).updatePermissionStatus(1L, request);
    }
}
