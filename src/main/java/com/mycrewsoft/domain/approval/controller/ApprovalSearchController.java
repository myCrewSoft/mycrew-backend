package com.mycrewsoft.domain.approval.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDocumentDetailResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDraftCountResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDraftSummaryResponse;
import com.mycrewsoft.domain.approval.service.ApprovalConstants;
import com.mycrewsoft.domain.approval.service.ApprovalSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Approval", description = "전자결재 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/approval")
public class ApprovalSearchController {

    private final ApprovalSearchService approvalSearchService;

    @Operation(summary = "전자결재 함별 기안서 건수 조회",
            description = "서브 사이드바 표시용으로 로그인한 사용자의 상신함(진행/완료/반려/임시저장)과 수신함(요청/내역/완료) 기안서 건수를 한 번에 조회합니다.")
    @GetMapping("/counts")
    public ResponseEntity<ApiResponse<ApprovalDraftCountResponse>> readMyDraftCounts() {
        return ResponseEntity.ok(ApiResponse.success(approvalSearchService.readMyDraftCounts()));
    }

    @Operation(summary = "내 기안서 목록 조회", description = "문서 상태와 검색어 조건으로 로그인한 사용자의 기안서 목록을 페이지 단위로 조회합니다.")
    @GetMapping("/drafts")
    public ResponseEntity<ApiResponse<List<ApprovalDraftSummaryResponse>>> readMyDrafts(
            @RequestParam(required = false) String documentStatus,
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 데이터 수", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Page<ApprovalDraftSummaryResponse> result = approvalSearchService.readMyDrafts(documentStatus, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result));
    }

    @Operation(summary = "진행 중인 기안서 목록 조회", description = "로그인한 사용자가 기안한 결재 진행 중 문서를 페이지 단위로 조회합니다.")
    @GetMapping("/drafts/progress")
    public ResponseEntity<ApiResponse<List<ApprovalDraftSummaryResponse>>> readMyProgressDrafts(
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ApprovalDraftSummaryResponse> result = approvalSearchService.readMyDrafts(ApprovalConstants.DOC_STATUS_IN_PROGRESS, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result));
    }

    @Operation(summary = "완료된 기안서 목록 조회", description = "로그인한 사용자가 기안한 결재 완료 문서를 페이지 단위로 조회합니다.")
    @GetMapping("/drafts/completed")
    public ResponseEntity<ApiResponse<List<ApprovalDraftSummaryResponse>>> readMyCompletedDrafts(
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ApprovalDraftSummaryResponse> result = approvalSearchService.readMyDrafts(ApprovalConstants.DOC_STATUS_COMPLETED, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result));
    }

    @Operation(summary = "반려된 기안서 목록 조회", description = "로그인한 사용자가 기안한 결재 반려 문서를 반려 사유와 함께 페이지 단위로 조회합니다.")
    @GetMapping("/drafts/rejected")
    public ResponseEntity<ApiResponse<List<ApprovalDraftSummaryResponse>>> readMyRejectedDrafts(
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ApprovalDraftSummaryResponse> result = approvalSearchService.readMyDrafts(ApprovalConstants.DOC_STATUS_REJECTED, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result));
    }

    @Operation(summary = "임시저장 기안서 목록 조회", description = "로그인한 사용자가 임시저장한 기안서를 페이지 단위로 조회합니다.")
    @GetMapping("/drafts/temporary")
    public ResponseEntity<ApiResponse<List<ApprovalDraftSummaryResponse>>> readMyTemporaryDrafts(
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ApprovalDraftSummaryResponse> result = approvalSearchService.readMyDrafts(ApprovalConstants.DOC_STATUS_TEMPORARY, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result));
    }

    @Operation(summary = "결재 문서 상세 조회", description = "기안자 또는 결재자가 결재 문서 상세 정보와 결재 상태를 조회합니다.")
    @GetMapping("/documents/{drftDocSn}")
    public ResponseEntity<ApiResponse<ApprovalDocumentDetailResponse>> readApprovalDocument(
            @PathVariable Long drftDocSn) {
        return ResponseEntity.ok(ApiResponse.success(approvalSearchService.readApprovalDocument(drftDocSn)));
    }

    @Operation(summary = "기안서 결재 상태 상세 조회", description = "기안자가 자신의 기안서 결재 상태를 상세 조회합니다.")
    @GetMapping("/drafts/{drftDocSn}/status")
    public ResponseEntity<ApiResponse<ApprovalDocumentDetailResponse>> readDraftApprovalStatus(
            @PathVariable Long drftDocSn) {
        return ResponseEntity.ok(ApiResponse.success(approvalSearchService.readDraftApprovalStatus(drftDocSn)));
    }

    @Operation(summary = "결재 요청 목록 조회", description = "로그인한 결재자가 현재 결재해야 할 문서 목록을 페이지 단위로 조회합니다.")
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<ApprovalDraftSummaryResponse>>> readMyApprovalRequests(
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ApprovalDraftSummaryResponse> result = approvalSearchService.readMyApprovalRequests(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result));
    }

    @Operation(summary = "결재 이력 조회", description = "로그인한 결재자가 승인 또는 반려 처리한 문서 이력을 페이지 단위로 조회합니다.")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ApprovalDraftSummaryResponse>>> readMyApprovalHistory(
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ApprovalDraftSummaryResponse> result = approvalSearchService.readMyApprovalHistory(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result));
    }

    @Operation(summary = "결재 완료 문서 조회", description = "로그인한 결재자가 승인 처리했고 최종 완료된 문서 목록을 페이지 단위로 조회합니다.")
    @GetMapping("/completed-documents")
    public ResponseEntity<ApiResponse<List<ApprovalDraftSummaryResponse>>> readMyCompletedApprovalDocuments(
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ApprovalDraftSummaryResponse> result = approvalSearchService.readMyCompletedApprovalDocuments(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result));
    }

    @Operation(summary = "결재자 문서 통합 검색", description = "결재 요청, 결재 이력, 완료 문서 중 지정한 목록 유형에서 페이지 단위로 검색합니다. listType은 request, history, completed를 사용합니다.")
    @GetMapping("/approver-documents")
    public ResponseEntity<ApiResponse<List<ApprovalDraftSummaryResponse>>> searchApprovalDocumentsForApprover(
            @RequestParam String listType,
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ApprovalDraftSummaryResponse> result =
                approvalSearchService.searchApprovalDocumentsForApprover(listType, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), result));
    }
}
