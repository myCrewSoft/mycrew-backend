package com.mycrewsoft.domain.project.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "프로젝트 목록 조회 응답 DTO")
public class ProjectListResponseDto {
	@Schema(description = "프로젝트 ID", example = "1")
    private Long projId;

    @Schema(description = "프로젝트명", example = "그룹웨어 개발 프로젝트")
    private String projNm;

    @Schema(description = "시작날짜", example = "2025-06-01")
    private LocalDate projBgngYmd;

    @Schema(description = "종료날짜", example = "2025-12-31")
    private LocalDate projEndYmd;

    @Schema(description = "프로젝트 상태코드", example = "02")
    private String projStatCd;

    @Schema(description = "프로젝트 장 이름", example = "홍길동")
    private String projLdrNm;
    
    @Schema(description = "프로젝트 진척률", example = "100")
    private Integer projPrgrsRt;
}
