package com.mycrewsoft.domain.project.service;

import java.util.List;

import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;

public interface AdminProjectScopeService {

    List<AdminScopeOptionResponseDTO> getProjectScopeOptions();
}
