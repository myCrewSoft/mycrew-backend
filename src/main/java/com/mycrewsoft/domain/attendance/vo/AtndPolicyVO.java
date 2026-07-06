package com.mycrewsoft.domain.attendance.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 근무 정책(TB_ATND_POLICY) VO.
 * 관리자가 설정하는 회사 근무 정책. 적용기간(EFF_BGN_YMD ~ EFF_END_YMD)으로 버전 관리한다.
 */
@Getter
@Setter
public class AtndPolicyVO {

	private Long atndPolicyId;			// 근무 정책 ID - PK
	private String policyNm;			// 정책명
	private String deptCd;				// 부서 코드(NULL=전사 공통)
	private String workBgnTm;			// 출근 기준 시각 'HH24:MI'
	private String workEndTm;			// 퇴근 기준 시각 'HH24:MI'
	private Integer breakMin;			// 휴게(분)
	private Integer lateGraceMin;		// 지각 허용(분)
	private Integer stdWorkMinDay;		// 1일 소정근로(분)
	private Integer stdWorkDaysWk;		// 주 소정근로일
	private Integer stdWorkMinWk;		// 주 소정근로(분)
	private Integer maxOtMinWk;			// 주 연장 한도(분)
	private Integer otUnitMin;			// 연장 인정 단위(분)
	private Double annualLeaveDef;		// 기본 연차(일)
	private LocalDate effBgnYmd;		// 적용 시작일
	private LocalDate effEndYmd;		// 적용 종료일(NULL=현재 유효)
	private String useYn;				// 사용 여부
	private Long frstRgtrId;			// 최초 등록자
	private LocalDateTime frstRegDt;	// 최초 등록 일시
	private Long lastMdfrId;			// 최종 수정자
	private LocalDateTime lastMdfcnDt;	// 최종 수정 일시
}
