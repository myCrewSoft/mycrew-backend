package com.mycrewsoft.domain.approval.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.approval.dto.request.ApprovalAiDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftJobResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftResponseDTO;
import com.mycrewsoft.domain.approval.service.ApprovalAiDraftService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Approval", description = "전자결재 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/approval")
public class ApprovalAiController {

    private final ApprovalAiDraftService approvalAiDraftService;

    @Operation(
            summary = "AI 기안서 초안 생성",
            description = "등록된 결재 양식과 사원 정보를 활용해 기안서 본문과 결재선을 생성하고 임시저장합니다.")
    @PostMapping("/ai/drafts")
    public ResponseEntity<ApiResponse<ApprovalAiDraftResponseDTO>> createAiDraft(
            @Valid @RequestBody ApprovalAiDraftRequestDTO request) {
        ApprovalAiDraftResponseDTO response = approvalAiDraftService.createDraft(request);
        return ResponseEntity.ok(ApiResponse.success("AI 기안서 초안이 임시저장되었습니다.", response));
    }

    @Operation(
            summary = "AI 기안서 초안 생성 작업 시작",
            description = "등록된 결재 양식과 사원 정보를 활용하는 AI 기안서 생성 작업을 비동기로 시작합니다.")
    @PostMapping("/ai/drafts/jobs")
    public ResponseEntity<ApiResponse<ApprovalAiDraftJobResponseDTO>> createAiDraftJob(
            @Valid @RequestBody ApprovalAiDraftRequestDTO request) {
        ApprovalAiDraftJobResponseDTO response = approvalAiDraftService.createDraftJob(request);
        return ResponseEntity.ok(ApiResponse.success("AI 기안서 생성 작업을 시작했습니다.", response));
    }

    @Operation(
            summary = "AI 기안서 초안 생성 작업 조회",
            description = "AI 기안서 생성 작업의 진행 상태와 완료된 임시저장 문서 정보를 조회합니다.")
    @GetMapping("/ai/drafts/jobs/{jobId}")
    public ResponseEntity<ApiResponse<ApprovalAiDraftJobResponseDTO>> getAiDraftJob(
            @PathVariable Long jobId) {
        ApprovalAiDraftJobResponseDTO response = approvalAiDraftService.getDraftJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
