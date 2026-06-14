package com.mycrewsoft.domain.attendance.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 일일 근태 기간 집계 결과 VO. Mapper의 집계 쿼리 결과를 담는다.
 */
@Getter
@Setter
public class AtndStatsAggVO {

	private Integer workMin;		// 실근무 합(분)
	private Integer normalWorkMin;	// 소정근로 인정 합(분)
	private Integer otMin;			// 연장근무 합(분)
	private Integer approvedOtMin;	// 승인근무 합(분)
	private Integer excessMin;		// 초과근무 합(분)
	private Integer lateCnt;		// 지각 횟수
	private Integer earlyLeaveCnt;	// 조퇴 횟수
	private Integer halfDayCnt;		// 반차 횟수
	private Double leaveUseDay;		// 사용 휴가 일수
}
