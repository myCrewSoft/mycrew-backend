package com.mycrewsoft.domain.mtng.controller;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.mtng.dto.request.AdminMtngListRequest;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngAnalyticsResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngDetailResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngListPageResponse;
import com.mycrewsoft.domain.mtng.dto.response.AdminMtngStatsResponse;
import com.mycrewsoft.domain.mtng.service.AdminMtngService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 회의 관리", description = "관리자 전용 회의 조회 및 관리 API")
@RestController
@RequestMapping("/admin/meetings")
@RequiredArgsConstructor
public class AdminMtngController {

    private final AdminMtngService adminMtngService;

    @Operation(summary = "전체 회의 목록 조회", description = "필터/검색 조건으로 전체 직원의 회의 목록을 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<AdminMtngListPageResponse>> getAdminMtngList(
            @ModelAttribute AdminMtngListRequest request) {
        AdminMtngListPageResponse response = adminMtngService.getAdminMtngList(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "회의 상세 조회", description = "회의 ID로 상세 정보와 참여자 목록을 조회합니다.")
    @GetMapping("/{mtngId}")
    public ResponseEntity<ApiResponse<AdminMtngDetailResponse>> getAdminMtngDetail(
            @PathVariable Long mtngId) {
        AdminMtngDetailResponse response = adminMtngService.getAdminMtngDetail(mtngId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "상단 요약 카드 조회", description = "오늘 예정, 진행중, 완료, AI 대기 건수를 조회합니다.")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminMtngStatsResponse>> getAdminMtngStats() {
        AdminMtngStatsResponse response = adminMtngService.getAdminMtngStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "통계 탭 데이터 조회", description = "유형별 비율, 월별 추이, 시간대별 집중도, 회의록 생성률을 조회합니다.")
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<AdminMtngAnalyticsResponse>> getAdminMtngAnalytics() {
        AdminMtngAnalyticsResponse response = adminMtngService.getAdminMtngAnalytics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "진행중 회의 강제 종료", description = "진행 중인 회의를 즉시 종료합니다.")
    @PatchMapping("/{mtngId}/force-end")
    public ResponseEntity<ApiResponse<Void>> forceEndMtng(
            @PathVariable Long mtngId) {
        adminMtngService.forceEndMtng(mtngId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}