package com.mycrewsoft.domain.mtng.controller;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.mtng.dto.mom.request.MtngMomUpdateRequest;
import com.mycrewsoft.domain.mtng.dto.mom.response.MtngMomResponse;
import com.mycrewsoft.domain.mtng.service.MtngMomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MeetingMinutes", description = "회의록 API")
@RestController
@RequestMapping("/meetings/{mtngId}/minutes")
@RequiredArgsConstructor
public class MtngMomController {

    private final MtngMomService mtngMomService;

    @Operation(summary = "회의록 작성 시작 (오프라인)", description = "오프라인 회의 참여자가 빈 회의록 작성을 시작합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createEmptyMom(@PathVariable Long mtngId) {
        Long momId = mtngMomService.createEmptyMom(mtngId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회의록 작성을 시작합니다.", momId));
    }

    @Operation(summary = "회의록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<MtngMomResponse>> getMom(@PathVariable Long mtngId) {
        MtngMomResponse response = mtngMomService.getMom(mtngId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "회의록 수정", description = "결재 진행 중이거나 완료된 회의록은 수정할 수 없습니다.")
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateMom(
            @PathVariable Long mtngId,
            @RequestBody MtngMomUpdateRequest request) {
        mtngMomService.updateMom(mtngId, request);
        return ResponseEntity.ok(ApiResponse.success("회의록이 수정되었습니다.", null));
    }

    @Operation(summary = "결재 요청", description = "전자결재 시스템에 회의록 결재를 요청합니다.")
    @PostMapping("/approval")
    public ResponseEntity<ApiResponse<Void>> requestApproval(@PathVariable Long mtngId) {
        mtngMomService.requestApproval(mtngId);
        return ResponseEntity.ok(ApiResponse.success("결재 요청이 발송되었습니다.", null));
    }
    
    // 회의록 AI 초안 재생성 (자동 생성 실패 시 수동 재시도)
    @Operation(summary = "회의록 AI 초안 재생성", description = "자동 생성 실패 시 수동으로 AI 초안을 다시 생성합니다.")
    @PostMapping("/regenerate")
    public ResponseEntity<ApiResponse<String>> regenerateAiDraft(
            @PathVariable Long mtngId) {

        mtngMomService.regenerateAiDraft(mtngId);
        return ResponseEntity.ok(ApiResponse.success("AI 초안 재생성을 요청했습니다."));
    }
}