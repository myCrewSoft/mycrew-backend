package com.mycrewsoft.domain.approval.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "결재 기안서 임시 작성 요청")
public class ApprovalDraftRequestDTO {

    @Schema(description = "기안문 일련번호. 기존 임시저장 문서를 수정할 때 사용합니다.", example = "1001")
    private Long drftDocSn;

    @Schema(description = "기안서 제목", example = "휴가 신청서")
    @Size(max = 256, message = "기안서 제목은 256자 이하이어야 합니다.")
    private String docTtl;

    @Schema(description = "결재 희망 일시", example = "2026-06-30T18:00:00")
    private LocalDateTime aprvlHopeDt;

    @Schema(description = "기안서 템플릿 코드", example = "VACATION")
    private String tmplatCd;

    @Schema(description = "첨부파일 ID", example = "1001")
    private Long atchFileId;

    @Schema(description = "결재 전문 내용")
    private String aprvlFullCn;

    @Schema(description = "결재 단계 목록")
    @Valid
    private List<ApprovalStepRequestDTO> approvalLines;
}
