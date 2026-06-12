package com.mycrewsoft.domain.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 근태 이력(일자별) 응답 DTO. 본인 근태 이력 테이블에 사용한다.
 */
@Getter
@Setter
@Schema(description = "근태 이력 응답 DTO")
public class AtndHistoryResponse {

	@Schema(description = "근무 일자", example = "2026-06-12")
	private LocalDate atndDt;

	@Schema(description = "출근 시각", example = "2026-06-12T09:00:00")
	private LocalDateTime wrkStartDtm;

	@Schema(description = "퇴근 시각", example = "2026-06-12T18:10:00")
	private LocalDateTime wrkEndDtm;

	@Schema(description = "실근무 시간(분)", example = "490")
	private Integer workMin;

	@Schema(description = "지각 시간(분)", example = "0")
	private Integer lateMin;

	@Schema(description = "연장근무 시간(분)", example = "10")
	private Integer otMin;

	@Schema(description = "근태 상태 코드", example = "W02")
	private String atndStatCd;

	@Schema(description = "근태 상태명", example = "정상")
	private String atndStatNm;
}
