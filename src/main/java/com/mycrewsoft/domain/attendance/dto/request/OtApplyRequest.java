package com.mycrewsoft.domain.attendance.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 초과근무 사전 신청 요청 DTO. 전자결재 기안과 결재선을 함께 받는다.
 */
@Getter
@Setter
@Schema(description = "초과근무 사전 신청 요청 DTO")
public class OtApplyRequest {

	@Schema(description = "초과근무 일자", example = "2026-06-20")
	@NotNull(message = "초과근무 일자는 필수입니다.")
	private LocalDate otYmd;

	@Schema(description = "시작 시각(HH:mm)", example = "18:00")
	@NotBlank(message = "시작 시각은 필수입니다.")
	@Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "시작 시각은 HH:mm 형식이어야 합니다.")
	private String otBgnTm;

	@Schema(description = "종료 시각(HH:mm)", example = "21:00")
	@NotBlank(message = "종료 시각은 필수입니다.")
	@Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "종료 시각은 HH:mm 형식이어야 합니다.")
	private String otEndTm;

	@Schema(description = "신청 사유", example = "긴급 배포 대응")
	private String reqRsn;

	// ── 전자결재 기안 항목 ──

	@Schema(description = "기안서 제목", example = "초과근무 신청서")
	@NotBlank(message = "제목은 필수입니다.")
	private String docTtl;

	@Schema(description = "결재 양식 코드")
	private String tmplatCd;

	@Schema(description = "결재 전문 내용(HTML)")
	private String aprvlFullCn;

	@Schema(description = "결재 희망 일시")
	private LocalDateTime aprvlHopeDt;

	@Schema(description = "첨부파일 ID")
	private Long atchFileId;

	@Schema(description = "결재선(결재 단계 목록)")
	@Valid
	@NotEmpty(message = "결재자는 최소 1명 이상 필요합니다.")
	private List<ApprovalStepRequestDTO> approvalLines;
}
