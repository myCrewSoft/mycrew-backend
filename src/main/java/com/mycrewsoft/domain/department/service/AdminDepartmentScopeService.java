package com.mycrewsoft.domain.department.service;

import java.util.List;

import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;

public interface AdminDepartmentScopeService {

    List<AdminScopeOptionResponseDTO> getDepartmentScopeOptions();
}
