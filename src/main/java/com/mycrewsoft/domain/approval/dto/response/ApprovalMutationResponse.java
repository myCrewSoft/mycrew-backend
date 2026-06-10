package com.mycrewsoft.domain.approval.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "결재 변경 처리 응답")
public class ApprovalMutationResponse {

    @Schema(description = "기안문 일련번호", example = "1001")
    private Long drftDocSn;

    @Schema(description = "결재 문서 상태 코드", example = "01")
    private String aprvlDocSttsCd;

    @Schema(description = "처리 메시지", example = "결재 요청이 완료되었습니다.")
    private String message;
}
