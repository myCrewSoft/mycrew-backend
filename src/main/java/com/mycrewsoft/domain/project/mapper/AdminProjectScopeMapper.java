package com.mycrewsoft.domain.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;

@Mapper
public interface AdminProjectScopeMapper {

    List<AdminScopeOptionResponseDTO> selectProjectScopeOptions();
}
