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
import lombok.Getter;
import lombok.Setter;

/**
 * 휴가 신청 요청 DTO.
 * 전자결재 기안(제목/양식/내용/결재선)에 휴가 전용 항목(종류/기간/사유)을 더해 제출하면,
 * 전자결재 문서를 생성·결재요청하고 동시에 휴가 신청을 연동한다.
 */
@Getter
@Setter
@Schema(description = "휴가 신청 요청 DTO")
public class LeaveApplyRequest {

	// ── 휴가 항목 ──

	@Schema(description = "휴가 종류 코드", example = "ANNUAL")
	@NotBlank(message = "휴가 종류는 필수입니다.")
	private String leaveTypeCd;

	@Schema(description = "휴가 시작일", example = "2026-06-20")
	@NotNull(message = "시작일은 필수입니다.")
	private LocalDate leaveBgnYmd;

	@Schema(description = "휴가 종료일", example = "2026-06-21")
	@NotNull(message = "종료일은 필수입니다.")
	private LocalDate leaveEndYmd;

	@Schema(description = "신청 사유", example = "개인 사유")
	private String reqRsn;

	// ── 전자결재 기안 항목 ──

	@Schema(description = "기안서 제목", example = "휴가 신청서")
	@NotBlank(message = "제목은 필수입니다.")
	private String docTtl;

	@Schema(description = "결재 양식 코드", example = "LEAVE_REQ")
	private String tmplatCd;

	@Schema(description = "결재 전문 내용(HTML)")
	private String aprvlFullCn;

	@Schema(description = "결재 희망 일시", example = "2026-06-19T18:00:00")
	private LocalDateTime aprvlHopeDt;

	@Schema(description = "첨부파일 ID")
	private Long atchFileId;

	@Schema(description = "결재선(결재 단계 목록)")
	@Valid
	@NotEmpty(message = "결재자는 최소 1명 이상 필요합니다.")
	private List<ApprovalStepRequestDTO> approvalLines;
}
