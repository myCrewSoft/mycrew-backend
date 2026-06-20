package com.mycrewsoft.domain.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;
import com.mycrewsoft.domain.project.vo.ProjectVO;

@Mapper
public interface AdminProjectScopeMapper {

    List<AdminScopeOptionResponseDTO> selectProjectScopeOptions();
    
    List<ProjectVO> selectAdminProjectList();
}
