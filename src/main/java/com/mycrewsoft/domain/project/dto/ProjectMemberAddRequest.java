package com.mycrewsoft.domain.project.dto;

import java.util.List;

import com.mycrewsoft.domain.projectmember.dto.ProjectInviteMemberRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "프로젝트 참여자 추가 요청 DTO")
public class ProjectMemberAddRequest {
	
	@Schema(description = "추가할 사원 목록")
	private List<ProjectInviteMemberRequest> addMemberList;
}
