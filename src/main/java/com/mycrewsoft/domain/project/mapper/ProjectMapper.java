package com.mycrewsoft.domain.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
	
	/**
	 * 프로젝트 상태코드 예정 -> 진행 중으로 자동 전환 (배치용)
	 * @return
	 */
	int updateProjStateToInProgress();
	
	/**
	 * 프로젝트 담당자 ID 조회
	 * @param projId
	 * @return
	 */
	Long selectProjectLeaderId(@Param("projId") Long projId);
	
}
