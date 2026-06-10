package com.mycrewsoft.domain.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Schema(description = "업무 수정 요청 DTO")
public class TaskUpdateRequest {

    @Size(max = 200)
    @Schema(description = "업무명", example = "결제 게이트웨이 UI 컴포넌트 개발 (수정)")
    private String taskNm;

    @Size(max = 2000)
    @Schema(description = "업무 상세내용")
    private String taskCn;

    @Schema(description = "업무담당자 사번", example = "1031")
    private Long taskMngrId;

    @Schema(description = "업무상태코드 (00:해야할일 / 01:진행중 / 02:완료 / 03:중단)", example = "01")
    private String taskStatCd;

    @Schema(description = "업무우선순위코드 (01:높음 / 02:중간 / 03:낮음)", example = "02")
    private String taskPriorityCd;

    @Schema(description = "중요도코드", example = "01")
    private String taskImprtncCd;

    @Min(0) @Max(100)
    @Schema(description = "진척률 (0~100)", example = "65")
    private Integer taskPrgrsSmry;

    @Schema(description = "업무 시작일시")
    private LocalDateTime taskBgngDt;

    @Schema(description = "업무 종료일시(마감일)")
    private LocalDateTime taskEndDt;

    @Schema(description = "업무 참여자 사번 목록 (기존 목록 전체 교체)")
    private List<Long> empIdList;
}