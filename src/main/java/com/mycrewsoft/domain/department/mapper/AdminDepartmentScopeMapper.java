package com.mycrewsoft.domain.department.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;

@Mapper
public interface AdminDepartmentScopeMapper {

    List<AdminScopeOptionResponseDTO> selectDepartmentScopeOptions();
}
