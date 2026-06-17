package com.mycrewsoft.domain.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 오늘 본인 근태 현황 응답 DTO. 출근/퇴근 버튼 활성화 판단에 사용한다.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "오늘 본인 근태 현황 응답 DTO")
public class AtndTodayResponse {

	@Schema(description = "근무 일자", example = "2026-06-12")
	private LocalDate atndDt;

	@Schema(description = "출근 여부", example = "true")
	private boolean checkedIn;

	@Schema(description = "퇴근 여부", example = "false")
	private boolean checkedOut;

	@Schema(description = "근태 상태 코드", example = "W01")
	private String atndStatCd;

	@Schema(description = "근태 상태명", example = "근무중")
	private String atndStatNm;

	@Schema(description = "출근 시각", example = "2026-06-12T09:00:00")
	private LocalDateTime wrkStartDtm;

	@Schema(description = "퇴근 시각", example = "null")
	private LocalDateTime wrkEndDtm;

	@Schema(description = "지각 시간(분)", example = "0")
	private Integer lateMin;

	@Schema(description = "실근무 시간(분)", example = "0")
	private Integer workMin;
}
