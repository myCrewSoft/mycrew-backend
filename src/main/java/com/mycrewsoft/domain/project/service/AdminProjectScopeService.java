package com.mycrewsoft.domain.project.service;

import java.util.List;

import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;
import com.mycrewsoft.domain.project.dto.AdminProjectListResponseDto;

public interface AdminProjectScopeService {

    List<AdminScopeOptionResponseDTO> getProjectScopeOptions();
    
    List<AdminProjectListResponseDto> getAdminProjectList();
}
