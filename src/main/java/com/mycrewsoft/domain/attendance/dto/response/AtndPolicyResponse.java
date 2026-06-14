package com.mycrewsoft.domain.attendance.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 근무 정책 조회 응답 DTO.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "근무 정책 조회 응답 DTO")
public class AtndPolicyResponse {

	@Schema(description = "근무 정책 ID", example = "1")
	private Long atndPolicyId;

	@Schema(description = "정책명", example = "2026년 표준 근무 정책")
	private String policyNm;

	@Schema(description = "출근 기준 시각(HH:mm)", example = "09:00")
	private String workBgnTm;

	@Schema(description = "퇴근 기준 시각(HH:mm)", example = "18:00")
	private String workEndTm;

	@Schema(description = "휴게 시간(분)", example = "60")
	private Integer breakMin;

	@Schema(description = "지각 허용 시간(분)", example = "0")
	private Integer lateGraceMin;

	@Schema(description = "1일 소정근로(분)", example = "480")
	private Integer stdWorkMinDay;

	@Schema(description = "주 소정근로일", example = "5")
	private Integer stdWorkDaysWk;

	@Schema(description = "주 소정근로(분)", example = "2400")
	private Integer stdWorkMinWk;

	@Schema(description = "주 연장근무 한도(분)", example = "720")
	private Integer maxOtMinWk;

	@Schema(description = "연장 인정 단위(분)", example = "30")
	private Integer otUnitMin;

	@Schema(description = "기본 연차(일)", example = "15")
	private Double annualLeaveDef;

	@Schema(description = "적용 시작일", example = "2026-01-01")
	private LocalDate effBgnYmd;

	@Schema(description = "적용 종료일(NULL=현재 유효)", example = "null")
	private LocalDate effEndYmd;
}
