package com.mycrewsoft.domain.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 관리자 전체 근태 현황 행 응답 DTO. 사원 × 일자 근태를 조회한다.
 */
@Getter
@Setter
@Schema(description = "관리자 전체 근태 현황 행 응답 DTO")
public class AdminAtndRowResponse {

	@Schema(description = "사원 ID", example = "1001")
	private Long empId;

	@Schema(description = "사원명", example = "임원호")
	private String empNm;

	@Schema(description = "부서명", example = "개발팀")
	private String deptNm;

	@Schema(description = "직급명", example = "대리")
	private String jbpsNm;

	@Schema(description = "근무 일자", example = "2026-06-12")
	private LocalDate atndDt;

	@Schema(description = "출근 시각", example = "2026-06-12T09:00:00")
	private LocalDateTime wrkStartDtm;

	@Schema(description = "퇴근 시각", example = "2026-06-12T18:10:00")
	private LocalDateTime wrkEndDtm;

	@Schema(description = "지각 시간(분)", example = "0")
	private Integer lateMin;

	@Schema(description = "실근무 시간(분)", example = "490")
	private Integer workMin;

	@Schema(description = "연장근무 시간(분)", example = "10")
	private Integer otMin;

	@Schema(description = "근태 상태 코드", example = "W02")
	private String atndStatCd;

	@Schema(description = "근태 상태명", example = "정상")
	private String atndStatNm;
}
