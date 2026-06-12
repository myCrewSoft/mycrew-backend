package com.mycrewsoft.domain.attendance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 사원별 연차 현황 응답 DTO.
 */
@Getter
@Setter
@Schema(description = "사원별 연차 현황 응답 DTO")
public class LeaveBalanceResponse {

	@Schema(description = "사원 ID", example = "1001")
	private Long empId;

	@Schema(description = "사원명", example = "임원호")
	private String empNm;

	@Schema(description = "부서명", example = "개발팀")
	private String deptNm;

	@Schema(description = "부여 일수", example = "15.0")
	private Double grantedDay;

	@Schema(description = "사용 일수", example = "2.5")
	private Double usedDay;

	@Schema(description = "잔여 일수", example = "12.5")
	private Double remainDay;
}
