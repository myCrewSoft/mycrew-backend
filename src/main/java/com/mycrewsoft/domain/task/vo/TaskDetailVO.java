package com.mycrewsoft.domain.task.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 업무 상세 조회용 VO
 * TB_TASK + TB_EMPLOYEE + TB_TASK_PTCPT + TB_DEPARTMENT JOIN 결과 매핑
 */
@Getter
@Setter
public class TaskDetailVO {

    private Long taskId;
    private Long projId;
    private String taskNm;
    private String taskCn;
    private String taskTypeCd;
    private Long taskMngrId;
    private String taskMngrNm;       // TB_EMPLOYEE JOIN - DB 컬럼 아님
    private Long prflImgFileId;      // TB_EMPLOYEE JOIN - DB 컬럼 아님
    private String deptNm;           // TB_DEPARTMENT JOIN - DB 컬럼 아님
    private String jobPstnNm;        // TB_JOB_POSITION JOIN - DB 컬럼 아님
    private String jobGrdNm;         // TB_JOB_GRADE JOIN - DB 컬럼 아님
    private String taskStatCd;
    private String taskPriorityCd;
    private String taskImprtncCd;
    private Integer taskPrgrsSmry;
    private LocalDateTime taskBgngDt;
    private LocalDateTime taskEndDt;
    private LocalDateTime frstRegDt;
    private Long frstRgtrId;
    private LocalDateTime lastMdfcnDt;

    private List<TaskPtcptDetailVO> ptcptList;  // 참여자 목록

    public List<Long> getRcvrEmpIds() {
        if (ptcptList == null || ptcptList.isEmpty()) {
            return List.of();
        }
        return ptcptList.stream()
                .map(TaskPtcptDetailVO::getEmpId)
                .toList();
    }
}