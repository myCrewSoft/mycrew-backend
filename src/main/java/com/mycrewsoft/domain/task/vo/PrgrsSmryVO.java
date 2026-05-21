package com.mycrewsoft.domain.task.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrgrsSmryVO {
	
    private Long taskId;		// 업무 ID		
    private Long projId;		// 프로젝트 ID

    private Integer totTaskCnt;		// 전체 업무 수
    private Integer cmplTaskCnt;	// 완료된 업무 수
    private Integer prgrsRt;		// 진척률

    private LocalDateTime calcDt;	// 마지막 계산 일시

}
