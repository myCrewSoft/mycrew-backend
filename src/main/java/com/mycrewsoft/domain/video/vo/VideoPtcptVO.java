package com.mycrewsoft.domain.video.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoPtcptVO {

	private Long ptcptId;			// 참여자 ID(TB_MEMBER) - PK
	
	private Long vconfId;			// 화상방 ID - FK
	
	private LocalDateTime joinDt;	// 참여 일시
	private LocalDateTime leavDt;	// 퇴장 일시
}
