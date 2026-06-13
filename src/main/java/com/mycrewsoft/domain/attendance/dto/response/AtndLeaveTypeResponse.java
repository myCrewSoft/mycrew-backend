package com.mycrewsoft.domain.attendance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 휴가 종류 응답 DTO.
 */
@Getter
@Setter
@Schema(description = "휴가 종류 응답 DTO")
public class AtndLeaveTypeResponse {

	@Schema(description = "휴가 종류 코드", example = "ANNUAL")
	private String leaveTypeCd;

	@Schema(description = "휴가 종류명", example = "연차")
	private String leaveTypeNm;

	@Schema(description = "차감 일수", example = "1.0")
	private Double deductDay;

	@Schema(description = "유급 여부", example = "Y")
	private String paidYn;

	@Schema(description = "반차 구분(A:오전/P:오후, 없으면 종일)", example = "A")
	private String halfDayCd;
}
