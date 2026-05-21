package com.mycrewsoft.domain.reservation.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfRmRsrvVO {

	private Long RsrvId;			// 회의실 예약 번호 ID - PK
	
	private Long ConfRmId;			// 회의실 ID - FK
	
	private Long RsrvMberId;		// 예약자 ID(TB_MEMBER)
	private String RsrvPurps;		// 예약 목적
	private LocalDateTime BeginDt;	// 시작 일시
	private LocalDateTime EenDt;	// 종료 일시
	private String RsrvSttusCd;		// 회의실 상태(01: 대기 / 02: 사용 중)
	private String IntgRsrvYn;		// 종일 예약 여부
}
