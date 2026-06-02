package com.mycrewsoft.domain.project.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;
import com.mycrewsoft.domain.project.service.AdminProjectScopeService;

class AdminProjectScopeControllerTest {

    @Test
    void getProjectScopeOptionsReturnsServiceResponse() {
        AdminProjectScopeService service = Mockito.mock(AdminProjectScopeService.class);
        AdminProjectScopeController controller = new AdminProjectScopeController(service);
        AdminScopeOptionResponseDTO option = new AdminScopeOptionResponseDTO("100", "100", "Migration");
        when(service.getProjectScopeOptions()).thenReturn(List.of(option));

        ApiResponse<List<AdminScopeOptionResponseDTO>> response = controller.getProjectScopeOptions();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsExactly(option);
        verify(service).getProjectScopeOptions();
    }
}
