package com.mycrewsoft.domain.project.service;

import java.util.List;

import com.mycrewsoft.domain.project.dto.ProjectCreateRequestDto;
import com.mycrewsoft.domain.project.dto.ProjectListResponseDto;

public interface ProjectService {
	/**
	 * 프로젝트 등록
	 * @param reqDto
	 */
	void createProject(ProjectCreateRequestDto reqDto);
	
	/**
	 * 프로젝트 목록 조회 (본인이 참여하는 목록만 조회)
	 * @return
	 */
	List<ProjectListResponseDto> getProjectList();
	
	/**
	 * 예정 → 진행 중 상태 자동 전환 (배치용)
	 */
	void updateProjStateToInProgress();
}
