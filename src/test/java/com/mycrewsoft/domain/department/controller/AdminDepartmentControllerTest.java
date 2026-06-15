package com.mycrewsoft.domain.department.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.department.dto.request.DepartmentDeleteRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentMemberAssignRequestDTO;
import com.mycrewsoft.domain.department.dto.response.AdminDepartmentResponseDTO;
import com.mycrewsoft.domain.department.dto.response.DepartmentMemberMutationResponseDTO;
import com.mycrewsoft.domain.department.service.AdminDepartmentService;

class AdminDepartmentControllerTest {

    @Test
    void getDepartmentListReturnsServiceResponse() {
        AdminDepartmentService service = Mockito.mock(AdminDepartmentService.class);
        AdminDepartmentController controller = new AdminDepartmentController(service);
        AdminDepartmentResponseDTO department = new AdminDepartmentResponseDTO();
        department.setDeptCd("DEPT_001");
        when(service.getDepartmentList()).thenReturn(List.of(department));

        ResponseEntity<ApiResponse<List<AdminDepartmentResponseDTO>>> response = controller.getDepartmentList();

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).containsExactly(department);
    }

    @Test
    void deleteDepartmentDelegatesReplacementRequestToService() {
        AdminDepartmentService service = Mockito.mock(AdminDepartmentService.class);
        AdminDepartmentController controller = new AdminDepartmentController(service);
        DepartmentDeleteRequestDTO request = new DepartmentDeleteRequestDTO();
        request.setReplacementDeptCd("DEPT_002");

        ResponseEntity<ApiResponse<String>> response = controller.deleteDepartment("DEPT_001", request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(service).deleteDepartment("DEPT_001", request);
    }

    @Test
    void assignDepartmentMembersReturnsMutationResponse() {
        AdminDepartmentService service = Mockito.mock(AdminDepartmentService.class);
        AdminDepartmentController controller = new AdminDepartmentController(service);
        DepartmentMemberAssignRequestDTO request = new DepartmentMemberAssignRequestDTO();
        request.setEmpIds(List.of(1001L, 1002L));
        DepartmentMemberMutationResponseDTO result = new DepartmentMemberMutationResponseDTO(null, "DEPT_001", 2);
        when(service.assignDepartmentMembers("DEPT_001", request)).thenReturn(result);

        ResponseEntity<ApiResponse<DepartmentMemberMutationResponseDTO>> response =
                controller.assignDepartmentMembers("DEPT_001", request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(result);
        verify(service).assignDepartmentMembers("DEPT_001", request);
    }
}
