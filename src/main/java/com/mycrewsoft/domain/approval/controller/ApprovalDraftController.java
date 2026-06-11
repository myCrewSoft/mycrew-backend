package com.mycrewsoft.domain.approval.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.approval.dto.request.ApprovalActionRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAvailabilityResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDocumentDetailResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDraftSummaryResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalMutationResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalTemplateResponse;
import com.mycrewsoft.domain.approval.service.ApprovalDraftWriteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Approval", description = "전자결재 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/approval")
public class ApprovalDraftController {

    private final ApprovalDraftWriteService approvalDraftService;

    @Operation(summary = "기안서 임시저장", description = "신규 기안서를 임시저장하거나 기존 임시저장 문서를 수정합니다.")
    @PostMapping("/drafts")
    public ResponseEntity<ApiResponse<Long>> saveTemporaryDraft(
            @Valid @RequestBody ApprovalDraftRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("기안서 임시저장 성공", approvalDraftService.saveTemporaryDraft(request)));
    }

    @Operation(summary = "결재선 저장", description = "임시저장 문서의 기존 결재선을 삭제하고 새 결재선을 저장합니다.")
    @PutMapping("/drafts/{drftDocSn}/lines")
    public ResponseEntity<ApiResponse<Void>> saveApprovalLine(
            @PathVariable Long drftDocSn,
            @Valid @RequestBody List<ApprovalStepRequestDTO> approvalSteps) {
        approvalDraftService.saveApprovalLine(drftDocSn, approvalSteps);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
