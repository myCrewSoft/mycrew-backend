package com.mycrewsoft.domain.task.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 업무 참여자 상세 조회용 VO
 * TB_TASK_PTCPT + TB_EMPLOYEE + TB_DEPARTMENT + TB_JOB_POSITION + TB_JOB_GRADE JOIN 결과 매핑
 */
@Getter
@Setter
public class TaskPtcptDetailVO {

    private Long taskPtcptId;
    private Long empId;
    private String empNm;            // TB_EMPLOYEE JOIN - DB 컬럼 아님
    private Long prflImgFileId;      // TB_EMPLOYEE JOIN - DB 컬럼 아님
    private String deptNm;           // TB_DEPARTMENT JOIN - DB 컬럼 아님
    private String jobPstnNm;        // TB_JOB_POSITION JOIN - DB 컬럼 아님
    private String jobGrdNm;         // TB_JOB_GRADE JOIN - DB 컬럼 아님
    private LocalDateTime joinDt;
}