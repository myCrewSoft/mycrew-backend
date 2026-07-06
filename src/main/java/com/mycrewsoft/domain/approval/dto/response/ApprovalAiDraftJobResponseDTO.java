package com.mycrewsoft.domain.approval.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전자결재 AI 기안서 생성 작업 응답")
public class ApprovalAiDraftJobResponseDTO {

    @Schema(description = "AI 작업 ID", example = "1001")
    private Long jobId;

    @Schema(description = "AI 작업 상태", example = "RUNNING")
    private String status;

    @Schema(description = "AI가 임시저장한 기안문 일련번호", example = "700")
    private Long drftDocSn;

    @Schema(description = "AI가 생성한 기안서 제목", example = "휴가 신청서")
    private String docTtl;

    @Schema(description = "AI 초안에 적용된 결재 양식 코드", example = "TMPL_AD2E39F3")
    private String tmplatCd;

    @Schema(description = "AI 초안 생성 중 서버가 보정한 내용 또는 사용자 확인이 필요한 경고")
    private List<String> warnings;

    @Schema(description = "AI 작업 실패 메시지")
    private String errorMessage;

    @Schema(description = "AI 작업 생성 일시")
    private LocalDateTime createdAt;

    @Schema(description = "AI 작업 최종 갱신 일시")
    private LocalDateTime updatedAt;
}
