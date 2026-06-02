package com.mycrewsoft.domain.task.service;

import java.util.List;

import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;

public interface AdminTaskScopeService {

    List<AdminScopeOptionResponseDTO> getTaskScopeOptions();
}
