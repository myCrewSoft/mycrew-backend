package com.mycrewsoft.domain.task.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 업무 관리 VO
 * 대응 테이블 : TB_TASK
 */
@Getter
@Setter
public class TaskVO {

	private Long taskId;             // 업무ID - PK
	
    private Long projId;             // 프로젝트ID - PK, FK
    private Long chtrmId;            // 채팅방ID - FK
    private String taskTypeCd;       // 업무유형코드 - FK
    private Long taskMngrId;         // 업무담당자ID
    private String taskStatCd;       // 업무상태
    private String taskPriorityCd;   // 업무우선순위
    private String taskNm;           // 업무명
    private String taskCn;           // 업무상세내용
    
    private LocalDateTime taskBgngDt;    // 시작일시
    private LocalDateTime taskEndDt;     // 종료일시
    private LocalDateTime frstRegDt;     // 최초등록일시
    private Long frstRgtrId;             // 최초등록자ID
    private LocalDateTime lastMdfcnDt;   // 최종수정일시
    private Long lastMdfrId;             // 최종수정자ID

    private String delYn;                // 삭제여부
    private LocalDateTime delDt;         // 삭제일시
    private Long deltrMbrId;             // 삭제자ID

    private Long taskAtchFileId;         // 업무첨부파일ID
    private Integer taskPrgrsSmry;		// 진척률
    private String taskImprtncCd;		// 중요도

    // has many
    private List<TaskPtcptVO> taskEmployeeList;   // 업무 참여자 목록
}