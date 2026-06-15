package com.mycrewsoft.domain.attendance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 관리자 기간별 사원 근태 집계 응답 DTO. 사원 1명의 기간 합계를 나타낸다.
 */
@Getter
@Setter
@Schema(description = "관리자 기간별 사원 근태 집계 응답 DTO")
public class AdminAtndStatResponse {

	@Schema(description = "사원 ID", example = "1001")
	private Long empId;

	@Schema(description = "사원명", example = "임원호")
	private String empNm;

	@Schema(description = "부서명", example = "개발팀")
	private String deptNm;

	@Schema(description = "직급명", example = "대리")
	private String jbpsNm;

	@Schema(description = "프로필 이미지 파일 ID", example = "14")
	private Long prflImgFileId;

	@Schema(description = "출근 일수", example = "5")
	private Integer presentDays;

	@Schema(description = "실근무 합계(분)", example = "2400")
	private Integer workMin;

	@Schema(description = "연장근무 합계(분)", example = "120")
	private Integer otMin;

	@Schema(description = "지각 횟수", example = "1")
	private Integer lateCnt;

	@Schema(description = "조퇴 횟수", example = "0")
	private Integer earlyLeaveCnt;

	@Schema(description = "휴가 일수 합계", example = "1.5")
	private Double leaveDays;
}
