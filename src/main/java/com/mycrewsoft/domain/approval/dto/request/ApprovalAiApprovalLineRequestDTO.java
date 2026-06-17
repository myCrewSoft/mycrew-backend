package com.mycrewsoft.domain.approval.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "전자결재 AI 결재선 자동 지정 요청")
public class ApprovalAiApprovalLineRequestDTO {

    @Schema(description = "AI에게 전달할 결재선 지정 요청 내용", example = "휴가 신청서에 맞는 결재선을 지정해줘.")
    @Size(max = 2000, message = "AI 결재선 요청 내용은 2000자 이하여야 합니다.")
    private String userPrompt;

    @Schema(description = "기안서 제목", example = "휴가 신청서")
    @Size(max = 200, message = "기안서 제목은 200자 이하여야 합니다.")
    private String docTtl;

    @Schema(description = "선택된 결재 양식 코드", example = "TMPL_AD2E39F3")
    @Size(max = 64, message = "결재 양식 코드는 64자 이하여야 합니다.")
    private String tmplatCd;

    @Schema(description = "작성 중인 기안서 HTML 본문")
    @Size(max = 20000, message = "기안서 본문은 20000자 이하여야 합니다.")
    private String aprvlFullCn;
}
