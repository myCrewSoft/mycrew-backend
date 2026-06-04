package com.mycrewsoft.domain.department.service;

import java.util.List;

import com.mycrewsoft.domain.department.dto.request.DepartmentCreateRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentDeleteRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentMemberAssignRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentMemberTransferRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentUpdateRequestDTO;
import com.mycrewsoft.domain.department.dto.response.AdminDepartmentMemberResponseDTO;
import com.mycrewsoft.domain.department.dto.response.AdminDepartmentResponseDTO;
import com.mycrewsoft.domain.department.dto.response.DepartmentMemberMutationResponseDTO;

public interface AdminDepartmentService {

    AdminDepartmentResponseDTO createDepartment(DepartmentCreateRequestDTO request);

    List<AdminDepartmentResponseDTO> getDepartmentList();

    AdminDepartmentResponseDTO getDepartment(String deptCd);

    AdminDepartmentResponseDTO updateDepartment(String deptCd, DepartmentUpdateRequestDTO request);

    void deleteDepartment(String deptCd, DepartmentDeleteRequestDTO request);

    List<AdminDepartmentMemberResponseDTO> getDepartmentMembers(String deptCd);

    DepartmentMemberMutationResponseDTO assignDepartmentMembers(String deptCd, DepartmentMemberAssignRequestDTO request);

    DepartmentMemberMutationResponseDTO transferDepartmentMembers(String deptCd, DepartmentMemberTransferRequestDTO request);
}
