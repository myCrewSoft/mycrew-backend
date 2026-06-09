package com.mycrewsoft.domain.projectmember.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.projectmember.vo.ProjectMemberVO;

@Mapper
public interface ProjectMemberMapper {
	/**
	 * 프로젝트 참여자 등록
	 * @param memberList
	 * @return
	 */
	int insertProjectMemberList(@Param("memberList") List<ProjectMemberVO> memberList);
}
