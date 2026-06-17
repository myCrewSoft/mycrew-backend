package com.mycrewsoft.domain.approval.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.approval.dto.request.ApprovalAiApprovalLineRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalAiDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiApprovalLineJobResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiContentJobResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftJobResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftResponseDTO;
import com.mycrewsoft.domain.approval.service.ApprovalAiDraftService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "전자결재 AI", description = "전자결재 AI 기안서 및 결재선 자동화 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/approval")
public class ApprovalAiController {

    private final ApprovalAiDraftService approvalAiDraftService;

    @Operation(
            summary = "AI 기안서 초안 생성",
            description = "등록된 결재 양식과 사원 정보를 사용해 기안서 본문과 결재선을 생성하고 임시저장합니다.")
    @PostMapping("/ai/drafts")
    public ResponseEntity<ApiResponse<ApprovalAiDraftResponseDTO>> createAiDraft(
            @Valid @RequestBody ApprovalAiDraftRequestDTO request) {
        ApprovalAiDraftResponseDTO response = approvalAiDraftService.createDraft(request);
        return ResponseEntity.ok(ApiResponse.success("AI 기안서 초안이 임시저장되었습니다.", response));
    }

    @Operation(
            summary = "AI 기안서 초안 생성 작업 시작",
            description = "등록된 결재 양식과 사원 정보를 사용하는 AI 기안서 생성 작업을 비동기로 시작합니다.")
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

    @Operation(
            summary = "AI 기안서 본문 생성 작업 시작",
            description = "등록된 결재 양식을 사용해 기안서 제목과 본문 HTML을 생성하는 작업을 비동기로 시작합니다. 생성 결과는 임시저장하지 않습니다.")
    @PostMapping("/ai/drafts/content/jobs")
    public ResponseEntity<ApiResponse<ApprovalAiContentJobResponseDTO>> createAiDraftContentJob(
            @Valid @RequestBody ApprovalAiDraftRequestDTO request) {
        ApprovalAiContentJobResponseDTO response = approvalAiDraftService.createDraftContentJob(request);
        return ResponseEntity.ok(ApiResponse.success("AI 기안서 본문 생성 작업을 시작했습니다.", response));
    }

    @Operation(
            summary = "AI 기안서 본문 생성 작업 조회",
            description = "AI 기안서 본문 생성 작업의 진행 상태와 완료된 제목, 양식 코드, HTML 본문을 조회합니다.")
    @GetMapping("/ai/drafts/content/jobs/{jobId}")
    public ResponseEntity<ApiResponse<ApprovalAiContentJobResponseDTO>> getAiDraftContentJob(
            @PathVariable Long jobId) {
        ApprovalAiContentJobResponseDTO response = approvalAiDraftService.getDraftContentJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "AI 기안서 양식 생성 후 임시저장 작업 시작",
            description = "AI가 기안서 제목과 본문 HTML을 생성한 뒤 결재선 없이 임시저장하는 비동기 작업을 시작합니다. 작업 완료 시 기존 알림으로 사용자에게 안내합니다.")
    @PostMapping("/ai/drafts/content-save/jobs")
    public ResponseEntity<ApiResponse<ApprovalAiDraftJobResponseDTO>> createAiDraftContentSaveJob(
            @Valid @RequestBody ApprovalAiDraftRequestDTO request) {
        ApprovalAiDraftJobResponseDTO response = approvalAiDraftService.createDraftContentSaveJob(request);
        return ResponseEntity.ok(ApiResponse.success("AI 기안서 양식 생성 및 임시저장 작업을 시작했습니다.", response));
    }

    @Operation(
            summary = "AI 기안서 양식 임시저장 작업 조회",
            description = "AI 기안서 양식 생성 및 임시저장 작업의 진행 상태와 완료된 임시저장 문서 정보를 조회합니다.")
    @GetMapping("/ai/drafts/content-save/jobs/{jobId}")
    public ResponseEntity<ApiResponse<ApprovalAiDraftJobResponseDTO>> getAiDraftContentSaveJob(
            @PathVariable Long jobId) {
        ApprovalAiDraftJobResponseDTO response = approvalAiDraftService.getDraftContentSaveJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "AI 결재선 자동 지정 작업 시작",
            description = "작성 중인 기안서 정보와 사원 정보를 사용해 결재선을 자동으로 지정하는 작업을 비동기로 시작합니다.")
    @PostMapping("/ai/approval-lines/jobs")
    public ResponseEntity<ApiResponse<ApprovalAiApprovalLineJobResponseDTO>> createAiApprovalLineJob(
            @Valid @RequestBody ApprovalAiApprovalLineRequestDTO request) {
        ApprovalAiApprovalLineJobResponseDTO response = approvalAiDraftService.createApprovalLineJob(request);
        return ResponseEntity.ok(ApiResponse.success("AI 결재선 자동 지정 작업을 시작했습니다.", response));
    }

    @Operation(
            summary = "AI 결재선 자동 지정 작업 조회",
            description = "AI 결재선 자동 지정 작업의 진행 상태와 완료된 결재 단계, 결재자 정보를 조회합니다.")
    @GetMapping("/ai/approval-lines/jobs/{jobId}")
    public ResponseEntity<ApiResponse<ApprovalAiApprovalLineJobResponseDTO>> getAiApprovalLineJob(
            @PathVariable Long jobId) {
        ApprovalAiApprovalLineJobResponseDTO response = approvalAiDraftService.getApprovalLineJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
