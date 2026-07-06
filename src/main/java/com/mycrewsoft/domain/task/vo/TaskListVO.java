package com.mycrewsoft.domain.task.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 업무 목록 조회용 VO
 * TB_TASK + TB_EMPLOYEE JOIN 결과 매핑
 */
@Getter
@Setter
public class TaskListVO {

    private Long taskId;
    private Long projId;
    private String taskNm;
    private String taskCn;
    private String taskTypeCd;
    private Long taskMngrId;
    private String taskMngrNm;       // TB_EMPLOYEE JOIN - DB 컬럼 아님
    private String taskMngrDeptNm;       // DB 컬럼 아님
    private String taskMngrJobGrdNm;       // TDB 컬럼 아님
    private Long prflImgFileId;
    private String taskStatCd;
    private String taskPriorityCd;
    private String taskImprtncCd;
    private Integer taskPrgrsSmry;
    private LocalDateTime taskBgngDt;
    private LocalDateTime taskEndDt;
}
