package com.mycrewsoft.domain.project.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "프로젝트 수정 요청 DTO")
public class ProjectUpdateRequestDto {
	@Schema(description = "프로젝트명", example = "글로벌 통합 정산 플랫폼 고도화")
	private String projNm;

    @Schema(description = "프로젝트 설명", example = "차세대 글로벌 결제 게이트웨이 구축")
    private String projCn;

    @Schema(description = "시작일", example = "2024-01-02")
    private LocalDate projBgngYmd;

    @Schema(description = "종료일", example = "2024-06-30")
    private LocalDate projEndYmd;

    @Schema(description = "상태코드 (01:예정 02:진행중 03:완료 04:중단)", example = "02")
    private String projStatCd;
}
