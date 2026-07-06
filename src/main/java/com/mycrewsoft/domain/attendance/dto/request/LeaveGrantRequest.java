package com.mycrewsoft.domain.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 관리자 연차 부여 요청 DTO.
 */
@Getter
@Setter
@Schema(description = "연차 부여 요청 DTO")
public class LeaveGrantRequest {

	@Schema(description = "대상 사원 ID", example = "1001")
	@NotNull(message = "대상 사원은 필수입니다.")
	private Long empId;

	@Schema(description = "귀속 연도", example = "2026")
	@NotNull(message = "귀속 연도는 필수입니다.")
	private Integer baseYear;

	@Schema(description = "부여 일수(양수=부여, 음수=조정 차감)", example = "15")
	@NotNull(message = "부여 일수는 필수입니다.")
	private Double days;

	@Schema(description = "비고", example = "2026년 연차 부여")
	private String remark;
}
