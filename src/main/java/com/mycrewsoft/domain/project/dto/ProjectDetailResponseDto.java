package com.mycrewsoft.domain.project.dto;

import java.time.LocalDate;
import java.util.List;

import com.mycrewsoft.domain.projectmember.dto.ProjectMemberResponseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "프로젝트 상세 조회 응답 DTO")
public class ProjectDetailResponseDto {
	@Schema(description = "프로젝트ID", example = "1")
    private Long projId;

    @Schema(description = "프로젝트명", example = "글로벌 통합 정산 플랫폼 고도화")
    private String projNm;

    @Schema(description = "프로젝트 상세내용", example = "차세대 글로벌 결제 게이트웨이 구축")
    private String projCn;

    @Schema(description = "시작날짜", example = "2024-01-02")
    private LocalDate projBgngYmd;

    @Schema(description = "종료날짜", example = "2024-06-30")
    private LocalDate projEndYmd;

    @Schema(description = "표시 상태코드 (01:예정 02:진행중 03:완료 04:중단)", example = "02")
    private String projStatCd;

    @Schema(description = "프로젝트 장 이름", example = "김철수")
    private String projLdrNm;

    @Schema(description = "진척률 (%)", example = "66")
    private Integer projPrgrsRt;

    @Schema(description = "현재 참여자 목록")
    private List<ProjectMemberResponseDto> projMemberList;
}
