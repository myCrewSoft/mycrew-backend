package com.mycrewsoft.domain.reservation.vo;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ConfRmRsrvVO {

	private Long rsrvId;			// 회의실 예약 번호 ID - PK
	
	private Long confRmId;			// 회의실 ID - FK
	
	private Long rsrvEmpId;		// 예약자 ID(TB_EMPLOYEE)
	private String rsrvPurps;		// 예약 목적
	private LocalDateTime beginDt;	// 시작 일시
	private LocalDateTime endDt;	// 종료 일시
	private String delYn;			// 삭제 여부
	private String allDayYn;		// 종일 예약 여부
}
