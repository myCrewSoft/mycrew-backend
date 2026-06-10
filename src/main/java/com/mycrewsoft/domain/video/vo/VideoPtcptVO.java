package com.mycrewsoft.domain.video.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoPtcptVO {

	private Long vconfPtcptId;		// 참여ID - PK
	
	private Long vconfId;			// 화상방 ID 
	
	private Long empId;  			//참여자 ID(TB_EMPLOYEE) 

	private LocalDateTime joinDt;	// 참여 일시
	private LocalDateTime leavDt;	// 퇴장 일시
}
