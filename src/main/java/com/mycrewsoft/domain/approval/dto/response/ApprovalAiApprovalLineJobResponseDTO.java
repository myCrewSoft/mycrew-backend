package com.mycrewsoft.domain.approval.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전자결재 AI 결재선 자동 지정 작업 응답")
public class ApprovalAiApprovalLineJobResponseDTO {

    @Schema(description = "AI 작업 ID", example = "1001")
    private Long jobId;

    @Schema(description = "AI 작업 상태", example = "RUNNING")
    private String status;

    @Schema(description = "AI가 지정한 결재 단계 목록")
    private List<ApprovalStepRequestDTO> approvalLines;

    @Schema(description = "AI가 지정한 결재자 상세 정보")
    private List<ApprovalAiApproverCandidateDTO> approvers;

    @Schema(description = "AI 결재선 지정 중 서버가 보정한 내용 또는 사용자 확인이 필요한 경고")
    private List<String> warnings;

    @Schema(description = "AI 작업 실패 메시지")
    private String errorMessage;

    @Schema(description = "AI 작업 생성 일시")
    private LocalDateTime createdAt;

    @Schema(description = "AI 작업 최종 갱신 일시")
    private LocalDateTime updatedAt;
}
