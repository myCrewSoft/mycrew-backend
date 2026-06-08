package com.mycrewsoft.domain.project.mapper;

import java.util.List;

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

	/**
	 * 본인 참여 프로젝트 목록 조회
	 * @param empId
	 * @return
	 */
	List<ProjectVO> selectProjectList(Long empId);
}
