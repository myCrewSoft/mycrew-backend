package com.mycrewsoft.domain.project.service;

import java.util.List;

import com.mycrewsoft.domain.project.dto.ProjectCreateRequestDto;
import com.mycrewsoft.domain.project.dto.ProjectDetailResponseDto;
import com.mycrewsoft.domain.project.dto.ProjectListResponseDto;
import com.mycrewsoft.domain.project.dto.ProjectMemberAddRequest;
import com.mycrewsoft.domain.project.dto.ProjectUpdateRequestDto;

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
	
	/**
	 * 프로젝트 상세 조회
	 * @param projId
	 * @return
	 */
	ProjectDetailResponseDto getProject(Long projId);
	
	/**
	 * 프로젝트 수정
	 * @param reqDto
	 */
	void modifyProject(Long projId, ProjectUpdateRequestDto updateReqDto);

	/**
	 * 프로젝트 참여자 추가
	 * @param projId
	 * @param reqDto
	 */
	void addProjMember(Long projId, ProjectMemberAddRequest reqDto);
	
	/**
	 * 프로젝트 참여자 단건 퇴출 
	 * @param projId
	 * @param empId
	 */
	void removeProjMember(Long projId, Long empId);
	
	// 프로젝트 채팅방 ID 입력
	void updateProjectChtrmId(Long projId, Long chtrmId);

	// 위젯용
	List<ProjectListResponseDto> getProjectListForWidget();

	// 관리자 대시보드 위젯용: 전체 프로젝트 상태별 집계
	com.mycrewsoft.domain.project.vo.ProjectStatusCountVO getProjectStatusCountsForWidget();
	
	//프로젝트 보고서 AI
	String buildAiReference(Long projId);

}
