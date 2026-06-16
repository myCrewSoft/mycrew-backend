package com.mycrewsoft.domain.video.vo;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VideoPtcptLogVO {

	private Long ptcptLogId;		// 입퇴장로그ID - PK
	private Long vconfId;			// 화상회의ID - FK
	private Long empId;			// 직원ID(TB_EMPLOYEE)
	private LocalDateTime joinDt;	// 입장 일시
	private LocalDateTime leavDt;	// 퇴장 일시
}