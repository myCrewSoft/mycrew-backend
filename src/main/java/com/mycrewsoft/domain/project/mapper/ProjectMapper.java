package com.mycrewsoft.domain.project.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.project.vo.ProjectVO;


@Mapper
public interface ProjectMapper {
	
	/**
	 * 프로젝트 등록
	 * @param projVo
	 * @return
	 */
	int insertProject(ProjectVO projVo);
}
