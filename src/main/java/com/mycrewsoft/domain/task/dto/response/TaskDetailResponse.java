package com.mycrewsoft.domain.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "업무 상세 조회 응답 DTO")
public class TaskDetailResponse {

    @Schema(description = "업무 ID", example = "1")
    private Long taskId;

    @Schema(description = "프로젝트 ID", example = "1")
    private Long projId;

    @Schema(description = "업무명", example = "결제 게이트웨이 UI 컴포넌트 개발")
    private String taskNm;

    @Schema(description = "업무 상세내용")
    private String taskCn;

    @Schema(description = "업무유형코드", example = "01")
    private String taskTypeCd;

    @Schema(description = "업무담당자 사번", example = "1022")
    private Long taskMngrId;

    @Schema(description = "업무담당자 이름 (JOIN)", example = "김민준")
    private String taskMngrNm;

    @Schema(description = "담당자 프로필 이미지 파일 ID")
    private Long prflImgFileId;

    @Schema(description = "담당자 부서명")
    private String deptNm;

    @Schema(description = "담당자 직책명")
    private String jobPstnNm;

    @Schema(description = "담당자 직급명")
    private String jobGrdNm;
    
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

    @Schema(description = "최초 등록일시")
    private LocalDateTime frstRegDt;

    @Schema(description = "최초 등록자 사번")
    private Long frstRgtrId;

    @Schema(description = "최종 수정일시")
    private LocalDateTime lastMdfcnDt;

    @Schema(description = "업무 참여자 목록")
    private List<TaskEmployeeResponse> employeeList;

    @Getter
    @Setter
    @Schema(description = "업무 참여자 정보")
    public static class TaskEmployeeResponse {

        @Schema(description = "참여자 사번", example = "1022")
        private Long empId;

        @Schema(description = "참여자 이름", example = "김민준")
        private String empNm;

        @Schema(description = "프로필 이미지 파일 ID")
        private Long prflImgFileId;

        @Schema(description = "부서명", example = "개발팀")
        private String deptNm;

        @Schema(description = "직책명")
        private String jobPstnNm;

        @Schema(description = "직급명")
        private String jobGrdNm;

        @Schema(description = "참여일시")
        private LocalDateTime joinDt;
    }
}