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
 * 출근/퇴근 처리 결과 응답 DTO.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "출근/퇴근 처리 결과 응답 DTO")
public class AtndCheckResponse {

	@Schema(description = "근무 일자", example = "2026-06-12")
	private LocalDate atndDt;

	@Schema(description = "근태 상태 코드", example = "W03")
	private String atndStatCd;

	@Schema(description = "근태 상태명", example = "지각")
	private String atndStatNm;

	@Schema(description = "출근 시각", example = "2026-06-12T09:12:00")
	private LocalDateTime wrkStartDtm;

	@Schema(description = "퇴근 시각(퇴근 시에만 채워짐)", example = "2026-06-12T18:30:00")
	private LocalDateTime wrkEndDtm;

	@Schema(description = "지각 시간(분)", example = "12")
	private Integer lateMin;

	@Schema(description = "조퇴 시간(분)", example = "0")
	private Integer earlyLeaveMin;

	@Schema(description = "실근무 시간(분)", example = "510")
	private Integer workMin;

	@Schema(description = "연장근무 시간(분)", example = "30")
	private Integer otMin;

	@Schema(description = "처리 결과 메시지", example = "출근 처리되었습니다.")
	private String message;
}
