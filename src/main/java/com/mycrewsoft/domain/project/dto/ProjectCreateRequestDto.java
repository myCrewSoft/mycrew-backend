package com.mycrewsoft.domain.project.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.mycrewsoft.domain.projectmember.dto.ProjectInviteMemberRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "프로젝트 생성 요청 dto")
public class ProjectCreateRequestDto {
	@Schema(description = "프로젝트명", example = "그룹웨어 개발 프로젝트")
    @NotBlank
    private String projNm;

    @Schema(description = "프로젝트 상세내용", example = "사내 그룹웨어 시스템 개발")
    private String projCn;

    @Schema(description = "프로젝트 시작날짜", example = "2025-06-01")
    @NotNull
    private LocalDate projBgngYmd;

    @Schema(description = "프로젝트 종료날짜", example = "2025-12-31")
    @NotNull
    private LocalDate projEndYmd;

    @Schema(description = "프로젝트 참여자 목록 (1명 이상)")
    @NotEmpty
    private List<ProjectInviteMemberRequest> projMemberList;
}
