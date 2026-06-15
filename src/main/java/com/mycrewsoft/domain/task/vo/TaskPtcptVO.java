package com.mycrewsoft.domain.task.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskPtcptVO {
	 
	private Long taskPtcptId;	 // 업무 참여ID - PK 
    private Long taskId;         // 업무ID - FK

    private Long empId;          // 업무 참여자ID
    
    private LocalDateTime joinDt;    // 참여일시
    private LocalDateTime leaveDt;   // 퇴장일시

}
