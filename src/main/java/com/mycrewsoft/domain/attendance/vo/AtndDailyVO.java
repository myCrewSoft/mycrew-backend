package com.mycrewsoft.domain.attendance.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 일일 근태(TB_ATND_DAILY) VO.
 * 사원·일자당 1행. 출퇴근 시각과 정책 기반 판정 결과(파생 지표)를 저장한다.
 */
@Getter
@Setter
public class AtndDailyVO {

	private Long empId;					// 사원 ID - PK
	private LocalDate atndDt;			// 근무 일자 - PK
	private String atndStatCd;			// 근태 상태 코드(FK→TB_ATND_SE_CD)
	private Long applyPolicyId;			// 판정에 적용된 정책 ID(스냅샷)
	private LocalDateTime wrkStartDtm;	// 실제 출근 시각
	private LocalDateTime wrkEndDtm;	// 실제 퇴근 시각(NULL=근무중)
	private String chkinIp;				// 출근 IP
	private String chkoutIp;			// 퇴근 IP
	private Integer lateMin;			// 지각(분)
	private Integer earlyLeaveMin;		// 조퇴(분)
	private Integer workMin;			// 실근무(분, 휴게 제외)
	private Integer normalWorkMin;		// 소정근로 인정(분)
	private Integer otMin;				// 연장근무(분)
	private Integer approvedOtMin;		// 승인근무(분)
	private Integer excessMin;			// 초과근무(분)
	private Double leaveDay;			// 그날 휴가 차감(0 / 0.5 / 1.0)
	private Long srcLeaveReqId;			// 휴가성일 때 신청 FK
	private LocalDateTime frstRegDt;	// 최초 등록 일시
	private LocalDateTime lastMdfcnDt;	// 최종 수정 일시
}
