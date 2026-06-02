package com.mycrewsoft.domain.department.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.department.service.AdminDepartmentScopeService;
import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;

class AdminDepartmentScopeControllerTest {

    @Test
    void getDepartmentScopeOptionsReturnsServiceResponse() {
        AdminDepartmentScopeService service = Mockito.mock(AdminDepartmentScopeService.class);
        AdminDepartmentScopeController controller = new AdminDepartmentScopeController(service);
        AdminScopeOptionResponseDTO option = new AdminScopeOptionResponseDTO("D001", "D001", "Development");
        when(service.getDepartmentScopeOptions()).thenReturn(List.of(option));

        ResponseEntity<ApiResponse<List<AdminScopeOptionResponseDTO>>> response = controller.getDepartmentScopeOptions();

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).containsExactly(option);
        verify(service).getDepartmentScopeOptions();
    }
}
