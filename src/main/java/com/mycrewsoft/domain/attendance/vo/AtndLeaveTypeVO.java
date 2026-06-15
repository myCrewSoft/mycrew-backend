package com.mycrewsoft.domain.attendance.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 휴가 종류(TB_ATND_LEAVE_TYPE) VO.
 */
@Getter
@Setter
public class AtndLeaveTypeVO {

	private String leaveTypeCd;		// 휴가 종류 코드 - PK
	private String leaveTypeNm;		// 휴가 종류명
	private Double deductDay;		// 차감 일수(연차1.0/반차0.5/병가0 등)
	private String paidYn;			// 유급 여부
	private String halfDayCd;		// 반차: A(오전)/P(오후), NULL=종일
	private String useYn;			// 사용 여부
	private Integer sortOrder;		// 정렬 순서
}
