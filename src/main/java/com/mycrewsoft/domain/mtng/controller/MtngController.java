package com.mycrewsoft.domain.mtng.controller;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.mtng.dto.request.MtngCreateRequest;
import com.mycrewsoft.domain.mtng.dto.request.MtngListRequest;
import com.mycrewsoft.domain.mtng.dto.request.MtngUpdateRequest;
import com.mycrewsoft.domain.mtng.dto.response.MtngDetailResponse;
import com.mycrewsoft.domain.mtng.dto.response.MtngListResponse;
import com.mycrewsoft.domain.mtng.service.MtngService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Meeting", description = "통합 회의 API")
@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MtngController {

    private final MtngService mtngService;

    @Operation(summary = "회의 생성", description = "회의 진행방식(온라인/오프라인/혼합)에 따라 화상회의 및 회의실 예약을 함께 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createMtng(@RequestBody MtngCreateRequest request) {
        Long mtngId = mtngService.createMtng(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회의가 생성되었습니다.", mtngId));
    }

    @Operation(summary = "회의 목록 조회", description = "내가 참여한 회의를 기간/키워드로 조회합니다. 키워드는 회의명, 회의록, 참여자명을 대상으로 합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MtngListResponse>>> getMtngList(MtngListRequest request) {
        List<MtngListResponse> response = mtngService.getMtngList(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "회의 상세 조회", description = "회의 기본정보, 참여자, 회의실, 화상회의, 회의록 상태를 함께 조회합니다.")
    @GetMapping("/{mtngId}")
    public ResponseEntity<ApiResponse<MtngDetailResponse>> getMtngDetail(@PathVariable Long mtngId) {
        MtngDetailResponse response = mtngService.getMtngDetail(mtngId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "회의 수정", description = "회의 정보, 진행방식, 참여자, 회의실 연결을 수정합니다. 시작된 회의는 수정할 수 없습니다.")
    @PutMapping("/{mtngId}")
    public ResponseEntity<ApiResponse<Void>> updateMtng(
            @PathVariable Long mtngId,
            @RequestBody MtngUpdateRequest request) {
        mtngService.updateMtng(mtngId, request);
        return ResponseEntity.ok(ApiResponse.success("회의가 수정되었습니다.", null));
    }

    @Operation(summary = "회의 삭제", description = "회의를 논리삭제합니다. 시작된 회의는 삭제할 수 없습니다.")
    @DeleteMapping("/{mtngId}")
    public ResponseEntity<ApiResponse<Void>> deleteMtng(@PathVariable Long mtngId) {
        mtngService.deleteMtng(mtngId);
        return ResponseEntity.ok(ApiResponse.success("회의가 삭제되었습니다.", null));
    }
}