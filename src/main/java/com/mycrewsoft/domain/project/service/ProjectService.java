package com.mycrewsoft.domain.project.service;

import com.mycrewsoft.domain.project.dto.ProjectCreateRequestDto;

public interface ProjectService {
	/**
	 * 프로젝트 등록
	 * @param reqDto
	 */
	void createProject(ProjectCreateRequestDto reqDto);
}
