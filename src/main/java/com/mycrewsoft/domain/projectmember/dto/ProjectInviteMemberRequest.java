package com.mycrewsoft.domain.projectmember.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "프로젝트 참여자 생성 요청")
public class ProjectInviteMemberRequest {
	
	@NotNull
	@Schema(description = "참여자 사원 ID", example = "1234(사번)")
	private Long empId;
}
