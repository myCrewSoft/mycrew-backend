package com.mycrewsoft.domain.task.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;

@Mapper
public interface AdminTaskScopeMapper {

    List<AdminScopeOptionResponseDTO> selectTaskScopeOptions();
}
