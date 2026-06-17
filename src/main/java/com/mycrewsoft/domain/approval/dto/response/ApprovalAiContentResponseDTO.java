package com.mycrewsoft.domain.approval.dto.response;

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
@Schema(description = "전자결재 AI 기안서 본문 생성 응답")
public class ApprovalAiContentResponseDTO {

    @Schema(description = "AI가 생성한 기안서 제목", example = "휴가 신청서")
    private String docTtl;

    @Schema(description = "AI 초안에 적용된 결재 양식 코드", example = "TMPL_AD2E39F3")
    private String tmplatCd;

    @Schema(description = "AI가 생성한 기안서 HTML 본문")
    private String aprvlFullCn;

    @Schema(description = "AI 본문 생성 중 서버가 보정한 내용 또는 사용자 확인이 필요한 경고")
    private List<String> warnings;
}
