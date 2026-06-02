package com.mycrewsoft.domain.task.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;
import com.mycrewsoft.domain.task.service.AdminTaskScopeService;

class AdminTaskScopeControllerTest {

    @Test
    void getTaskScopeOptionsReturnsServiceResponse() {
        AdminTaskScopeService service = Mockito.mock(AdminTaskScopeService.class);
        AdminTaskScopeController controller = new AdminTaskScopeController(service);
        AdminScopeOptionResponseDTO option = new AdminScopeOptionResponseDTO("200", "200", "API Work");
        when(service.getTaskScopeOptions()).thenReturn(List.of(option));

        ApiResponse<List<AdminScopeOptionResponseDTO>> response = controller.getTaskScopeOptions();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsExactly(option);
        verify(service).getTaskScopeOptions();
    }
}
