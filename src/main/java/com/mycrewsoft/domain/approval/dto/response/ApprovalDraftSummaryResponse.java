package com.mycrewsoft.domain.approval.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "결재 기안서 목록 응답")
public class ApprovalDraftSummaryResponse {

    @Schema(description = "기안문 일련번호", example = "1001")
    private Long drftDocSn;

    @Schema(description = "기안서 제목", example = "휴가 신청서")
    private String docTtl;

    @Schema(description = "템플릿 코드", example = "VACATION")
    private String tmplatCd;

    @Schema(description = "기안자 사원 ID", example = "1111")
    private Long empId;

    @Schema(description = "기안자명", example = "홍길동")
    private String drafterEmpNm;

    @Schema(description = "결재자명 목록", example = "김승인, 이검토")
    private String approverNames;

    @Schema(description = "기안 요청 일시", example = "2026-06-09T10:30:00")
    private LocalDateTime drftReqstDt;

    @Schema(description = "결재 희망 일시", example = "2026-06-30T18:00:00")
    private LocalDateTime aprvlHopeDt;

    @Schema(description = "결재 문서 상태 코드", example = "01")
    private String aprvlDocSttsCd;

    @Schema(description = "반려 사유")
    private String rtrnRsn;
}
