package com.mycrewsoft.domain.attendance.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 휴가 신청(TB_ATND_LEAVE_REQ) VO. 전자결재 문서(DRFT_DOC_SN)와 연동된다.
 */
@Getter
@Setter
public class AtndLeaveReqVO {

	private Long leaveReqId;			// 휴가 신청 ID - PK
	private Long empId;					// 신청 사원 ID
	private String leaveTypeCd;			// 휴가 종류 코드
	private Long drftDocSn;				// 전자결재 문서 번호(FK)
	private LocalDateTime leaveBgnDtm;	// 시작 일시
	private LocalDateTime leaveEndDtm;	// 종료 일시
	private Double leaveDayCnt;			// 총 차감 일수
	private String reqRsn;				// 신청 사유
	private String aprvlSttusCd;		// 01대기/02승인/03반려
	private String rflctYn;				// 근태 취합 반영 여부
	private LocalDateTime rflctDt;		// 반영 일시
	private LocalDateTime frstRegDt;	// 최초 등록 일시
}
