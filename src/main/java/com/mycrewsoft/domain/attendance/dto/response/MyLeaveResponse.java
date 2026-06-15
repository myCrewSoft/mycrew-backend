package com.mycrewsoft.domain.attendance.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 내 휴가 신청 내역 응답 DTO.
 */
@Getter
@Setter
@Schema(description = "내 휴가 신청 내역 응답 DTO")
public class MyLeaveResponse {

	@Schema(description = "휴가 신청 ID", example = "1")
	private Long leaveReqId;

	@Schema(description = "전자결재 문서 번호", example = "1001")
	private Long drftDocSn;

	@Schema(description = "휴가 종류명", example = "연차")
	private String leaveTypeNm;

	@Schema(description = "시작 일시", example = "2026-06-20T00:00:00")
	private LocalDateTime leaveBgnDtm;

	@Schema(description = "종료 일시", example = "2026-06-21T23:59:00")
	private LocalDateTime leaveEndDtm;

	@Schema(description = "총 차감 일수", example = "2.0")
	private Double leaveDayCnt;

	@Schema(description = "신청 사유", example = "개인 사유")
	private String reqRsn;

	@Schema(description = "결재 상태 코드(01대기/02승인/03반려)", example = "01")
	private String aprvlSttusCd;

	@Schema(description = "근태 반영 여부", example = "N")
	private String rflctYn;

	@Schema(description = "신청 일시", example = "2026-06-12T10:00:00")
	private LocalDateTime frstRegDt;
}
