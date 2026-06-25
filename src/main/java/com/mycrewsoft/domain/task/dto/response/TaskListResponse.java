package com.mycrewsoft.domain.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "업무 목록 조회 응답 DTO")
public class TaskListResponse {

    @Schema(description = "업무 ID", example = "1")
    private Long taskId;

    @Schema(description = "프로젝트 ID", example = "1")
    private Long projId;

    @Schema(description = "업무명", example = "결제 게이트웨이 UI 컴포넌트 개발")
    private String taskNm;
    
    @Schema(description = "업무 상세내용", example = "글로벌 결제 승인 화면에 사용할 공통 컴포넌트를 구현합니다.")
    private String taskCn;

    @Schema(description = "업무유형코드", example = "01")
    private String taskTypeCd;

    @Schema(description = "업무담당자 사번", example = "1022")
    private Long taskMngrId;

    @Schema(description = "업무담당자 이름 (JOIN)", example = "김민준")
    private String taskMngrNm;
    
    @Schema(description = "업무담당자 부서 (JOIN)", example = "개발부")
    private String taskMngrDeptNm;
    
    @Schema(description = "업무담당자 직급 (JOIN)", example = "팀장")
    private String taskMngrJobGrdNm;

    @Schema(description = "담당자 프로필 이미지 파일 ID")
    private Long prflImgFileId;
    
    @Schema(description = "업무상태코드", example = "01")
    private String taskStatCd;

    @Schema(description = "업무우선순위코드", example = "01")
    private String taskPriorityCd;

    @Schema(description = "중요도코드", example = "01")
    private String taskImprtncCd;

    @Schema(description = "진척률 (0~100)", example = "65")
    private Integer taskPrgrsSmry;

    @Schema(description = "업무 시작일시")
    private LocalDateTime taskBgngDt;
    
    @Schema(description = "업무 종료일시(마감일)")
    private LocalDateTime taskEndDt;
}