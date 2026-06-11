package com.mycrewsoft.domain.projectmember.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "프로젝트 참여자 응답 DTO")
public class ProjectMemberResponseDto {
	
	@Schema(description = "사원ID", example = "1001")
    private Long empId;

    @Schema(description = "사원명", example = "이지혜")
    private String empNm;

    @Schema(description = "부서명", example = "개발팀")
    private String deptNm;
}