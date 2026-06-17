package com.mycrewsoft.domain.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 내 초과근무 신청 내역 응답 DTO.
 */
@Getter
@Setter
@Schema(description = "내 초과근무 신청 내역 응답 DTO")
public class MyOtResponse {

	@Schema(description = "초과근무 신청 ID", example = "1")
	private Long otReqId;

	@Schema(description = "전자결재 문서 번호", example = "1002")
	private Long drftDocSn;

	@Schema(description = "초과근무 일자", example = "2026-06-20")
	private LocalDate otDt;

	@Schema(description = "시작 일시", example = "2026-06-20T18:00:00")
	private LocalDateTime otBgnDtm;

	@Schema(description = "종료 일시", example = "2026-06-20T21:00:00")
	private LocalDateTime otEndDtm;

	@Schema(description = "승인 시간(분)", example = "180")
	private Integer approvedMin;

	@Schema(description = "결재 상태 코드(01대기/02승인/03반려)", example = "01")
	private String aprvlSttusCd;

	@Schema(description = "근태 반영 여부", example = "N")
	private String rflctYn;
}
