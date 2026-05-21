package com.mycrewsoft.domain.task.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskMemberVO {
	 
    private Long mbrId;          // 회원ID - PK
    private Long taskId;         // 업무ID - PK, FK
    private Long projId;         // 프로젝트ID - PK, FK

    private LocalDateTime joinDt;    // 참여일시
    private LocalDateTime leaveDt;   // 퇴장일시

}
