package com.mycrewsoft.domain.project.dto;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "관리자용 프로젝트 목록 응답 DTO")
public class AdminProjectListResponseDto {

    @Schema(description = "프로젝트 ID")
    private Long projId;

    @Schema(description = "프로젝트명")
    private String projNm;

    @Schema(description = "시작일")
    private LocalDate projBgngYmd;

    @Schema(description = "종료일")
    private LocalDate projEndYmd;

    @Schema(description = "상태코드 (01:예정 02:진행중 03:완료 04:중단)")
    private String projStatCd;

    @Schema(description = "프로젝트 장 이름")
    private String projLdrNm;

    @Schema(description = "프로젝트 장 사번")
    private Long projLdrEmpId;

    @Schema(description = "프로젝트 진척률 (0~100)")
    private Integer projPrgrsRt;

    @Schema(description = "현재 참여자 수")
    private Integer memberCnt;

    @Schema(description = "기간 초과 위험 여부 (Y/N) — 진행중이고 종료일 7일 이내")
    private String deadlineRisk;
}