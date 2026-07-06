package com.mycrewsoft.domain.approval.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 전자결재 서브 사이드바 각 함의 기안서 건수 응답.
 * 로그인한 사용자 기준으로 상신함/수신함 항목별 문서 수를 집계한다.
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "전자결재 함별 기안서 건수 응답")
public class ApprovalDraftCountResponse {

    @Schema(description = "상신함 - 진행 중 기안서 수", example = "3")
    private long sentProgress;

    @Schema(description = "상신함 - 완료된 기안서 수", example = "12")
    private long sentCompleted;

    @Schema(description = "상신함 - 반려된 기안서 수", example = "1")
    private long sentRejected;

    @Schema(description = "상신함 - 임시저장 기안서 수", example = "2")
    private long sentTemporary;

    @Schema(description = "수신함 - 결재 요청(내 처리 대기) 문서 수", example = "5")
    private long receivedRequests;

    @Schema(description = "수신함 - 결재 내역(내가 처리한) 문서 수", example = "20")
    private long receivedHistory;

    @Schema(description = "수신함 - 결재 완료(내가 승인한 최종 완료) 문서 수", example = "15")
    private long receivedCompleted;
}
