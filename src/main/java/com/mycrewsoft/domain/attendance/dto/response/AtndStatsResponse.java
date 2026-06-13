package com.mycrewsoft.domain.attendance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 근태 통계 지표 응답 DTO.
 * 기간(일/주/월/년)별 집계값과, 항상 '이번 주' 기준의 잔여 근무/연장, 귀속연도 잔여 연차를 함께 제공한다.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "근태 통계 지표 응답 DTO")
public class AtndStatsResponse {

	@Schema(description = "조회 기간 구분(DAY/WEEK/MONTH/YEAR)", example = "WEEK")
	private String period;

	@Schema(description = "기간 라벨", example = "2026-06-08 ~ 2026-06-14")
	private String periodLabel;

	@Schema(description = "실근무 시간(분)", example = "1920")
	private Integer workMin;

	@Schema(description = "소정근로 인정 시간(분)", example = "1800")
	private Integer normalWorkMin;

	@Schema(description = "연장근무 시간(분)", example = "120")
	private Integer otMin;

	@Schema(description = "승인근무 시간(분)", example = "90")
	private Integer approvedOtMin;

	@Schema(description = "초과근무 시간(분)", example = "30")
	private Integer excessMin;

	@Schema(description = "지각 횟수", example = "1")
	private Integer lateCnt;

	@Schema(description = "조퇴 횟수", example = "0")
	private Integer earlyLeaveCnt;

	@Schema(description = "반차 횟수", example = "1")
	private Integer halfDayCnt;

	@Schema(description = "사용 휴가 일수(기간 내)", example = "1.5")
	private Double leaveUseDay;

	// --- 항상 '이번 주' 기준 / 연 단위 잔여 지표 ---

	@Schema(description = "이번 주 잔여 근무시간(분)", example = "480")
	private Integer remainingWorkMin;

	@Schema(description = "이번 주 잔여 연장근무(분)", example = "600")
	private Integer remainingOtMin;

	@Schema(description = "주 소정근로 한도(분)", example = "2400")
	private Integer stdWorkMinWk;

	@Schema(description = "주 연장근무 한도(분)", example = "720")
	private Integer maxOtMinWk;

	@Schema(description = "귀속연도 잔여 연차(일)", example = "12.5")
	private Double remainAnnualLeave;

	@Schema(description = "정책상 기본 연차(일)", example = "15")
	private Double annualLeaveDef;

	@Schema(description = "귀속연도 사용 연차(일)", example = "2.5")
	private Double usedAnnualLeave;
}
