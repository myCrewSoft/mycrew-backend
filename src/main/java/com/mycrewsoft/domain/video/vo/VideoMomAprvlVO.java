package com.mycrewsoft.domain.video.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoMomAprvlVO {

	private Long aprvlId;       // 결재 ID - PK

	private Long momId;			// 화의록 ID
	private Long ptcptId;		// 결재자 ID(TB_EMPLOYEE)

	private String aprvlSttusCd;	// 결재 상태 코드
	
	private LocalDateTime aprvlDt;	// 결재 일시
	private LocalDateTime creatDt;	// 생성 일시
	
	private Integer aprvlRoundNo;	// 결제 회차

}
