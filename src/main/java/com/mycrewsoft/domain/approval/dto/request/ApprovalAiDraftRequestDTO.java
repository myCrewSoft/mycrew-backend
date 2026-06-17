package com.mycrewsoft.domain.approval.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "전자결재 AI 기안서 초안 생성 요청")
public class ApprovalAiDraftRequestDTO {

    @Schema(description = "AI에게 전달할 기안서 작성 요청 내용", example = "6월 20일부터 21일까지 개인 사유로 휴가 신청서 작성해줘.")
    @NotBlank(message = "AI 기안 요청 내용은 필수입니다.")
    @Size(max = 2000, message = "AI 기안 요청 내용은 2000자 이하이어야 합니다.")
    private String userPrompt;

    @Schema(description = "우선 활용할 결재 양식 코드. 비워두면 AI가 사용 가능한 양식 중 선택합니다.", example = "TMPL_AD2E39F3")
    @Size(max = 64, message = "결재 양식 코드는 64자 이하이어야 합니다.")
    private String tmplatCd;
}
