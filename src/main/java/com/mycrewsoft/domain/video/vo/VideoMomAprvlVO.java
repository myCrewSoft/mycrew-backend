package com.mycrewsoft.domain.video.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoMomAprvlVO {

	private Long ptcptId;		// 결제자 ID(TB_MEMBER) - PK
	private Long momId;			// 화의록 ID - FK

	private Long aprvlSttusCd;	// 결제 상태 코드
	
	private LocalDateTime aprvlDt;	// 결재 일시
	private LocalDateTime creatDt;	// 생성 일시
	
	private Integer aprvlRoundNo;	// 결제 회차

}
