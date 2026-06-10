package com.mycrewsoft.domain.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Schema(description = "업무 생성 요청 DTO")
public class TaskCreateRequest {

    @NotNull
    @Schema(description = "프로젝트 ID (개인 업무이면 null 허용)", example = "1")
    private Long projId;

    @NotBlank
    @Size(max = 200)
    @Schema(description = "업무명", example = "결제 게이트웨이 UI 컴포넌트 개발")
    private String taskNm;

    @Size(max = 2000)
    @Schema(description = "업무 상세내용", example = "글로벌 결제 승인 화면에 사용할 공통 컴포넌트를 구현합니다.")
    private String taskCn;

    @NotBlank
    @Schema(description = "업무유형코드 (TaskType 코드 테이블 참조)", example = "01")
    private String taskTypeCd;
    
    @NotBlank
    @Schema(description = "업무 상태 코드", example = "01")
    private String taskStatCd;

    @NotNull
    @Schema(description = "업무담당자 사번", example = "1022")
    private Long taskMngrId;

    @NotBlank
    @Schema(description = "업무우선순위코드 (01:높음 / 02:중간 / 03:낮음)", example = "01")
    private String taskPriorityCd;

    @NotBlank
    @Schema(description = "중요도코드", example = "01")
    private String taskImprtncCd;

    @Schema(description = "업무 시작일시")
    private LocalDateTime taskBgngDt;

    @Schema(description = "업무 종료일시(마감일)")
    private LocalDateTime taskEndDt;

    @Schema(description = "업무 참여자 사번 목록")
    private List<Long> empIdList;
}