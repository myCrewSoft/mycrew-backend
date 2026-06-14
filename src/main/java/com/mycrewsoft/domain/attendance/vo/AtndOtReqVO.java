package com.mycrewsoft.domain.attendance.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 초과근무(연장) 사전 신청(TB_ATND_OT_REQ) VO. 전자결재 문서와 연동된다.
 */
@Getter
@Setter
public class AtndOtReqVO {

	private Long otReqId;				// 초과근무 신청 ID - PK
	private Long empId;					// 신청 사원 ID
	private LocalDate otDt;				// 초과근무 일자
	private LocalDateTime otBgnDtm;		// 시작 일시
	private LocalDateTime otEndDtm;		// 종료 일시
	private Integer approvedMin;		// 승인된 연장 시간(분)
	private Long drftDocSn;				// 전자결재 문서 번호(FK)
	private String aprvlSttusCd;		// 01대기/02승인/03반려
	private String rflctYn;				// 근태 반영 여부
}
