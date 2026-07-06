package com.mycrewsoft.domain.approval.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.approval.dto.request.ApprovalActionRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAvailabilityResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalMutationResponse;
import com.mycrewsoft.domain.approval.service.ApprovalRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Approval", description = "전자결재 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/approval")
public class ApprovalRequestController {
    private final ApprovalRequestService approvalDraftService;
	
	@Operation(summary = "결재 요청", description = "임시저장 문서를 결재 진행 상태로 전환하고 첫 번째 결재 단계를 진행 중으로 변경합니다.")
    @PostMapping("/drafts/{drftDocSn}/submit")
    public ResponseEntity<ApiResponse<ApprovalMutationResponse>> submitApproval(@PathVariable Long drftDocSn) {
        return ResponseEntity.ok(ApiResponse.success(approvalDraftService.submitApproval(drftDocSn)));
    }

    @Operation(summary = "결재 회수", description = "아직 승인 또는 반려 처리되지 않은 진행 중 문서를 회수합니다.")
    @PostMapping("/drafts/{drftDocSn}/withdraw")
    public ResponseEntity<ApiResponse<ApprovalMutationResponse>> withdrawApproval(@PathVariable Long drftDocSn) {
        return ResponseEntity.ok(ApiResponse.success(approvalDraftService.withdrawApproval(drftDocSn)));
    }
    
    @Operation(summary = "결재 승인", description = "현재 진행 중인 결재 단계의 결재자가 문서를 승인합니다.")
    @PostMapping("/documents/{drftDocSn}/approve")
    public ResponseEntity<ApiResponse<ApprovalMutationResponse>> approveApproval(
            @PathVariable Long drftDocSn,
            @Valid @RequestBody(required = false) ApprovalActionRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(approvalDraftService.approveApproval(drftDocSn, request)));
    }

    @Operation(summary = "결재 반려", description = "현재 진행 중인 결재 단계의 결재자가 반려 사유를 입력하고 문서를 반려합니다.")
    @PostMapping("/documents/{drftDocSn}/reject")
    public ResponseEntity<ApiResponse<ApprovalMutationResponse>> rejectApproval(
            @PathVariable Long drftDocSn,
            @Valid @RequestBody ApprovalActionRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(approvalDraftService.rejectApproval(drftDocSn, request)));
    }
    
    @Operation(summary = "승인 가능 여부 확인", description = "로그인한 사용자가 현재 진행 중인 결재 단계에서 승인할 수 있는지 확인합니다.")
    @GetMapping("/documents/{drftDocSn}/approve")
    public ResponseEntity<ApiResponse<ApprovalAvailabilityResponse>> canApprove(@PathVariable Long drftDocSn) {
        return ResponseEntity.ok(ApiResponse.success(approvalDraftService.canApprove(drftDocSn)));
    }

    @Operation(summary = "반려 가능 여부 확인", description = "로그인한 사용자가 현재 진행 중인 결재 단계에서 반려할 수 있는지 확인합니다.")
    @GetMapping("/documents/{drftDocSn}/reject")
    public ResponseEntity<ApiResponse<ApprovalAvailabilityResponse>> canReject(@PathVariable Long drftDocSn) {
        return ResponseEntity.ok(ApiResponse.success(approvalDraftService.canReject(drftDocSn)));
    }
}
