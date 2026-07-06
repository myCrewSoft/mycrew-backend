package com.mycrewsoft.domain.schedule.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.schedule.dto.request.AdminSchdRequest;
import com.mycrewsoft.domain.schedule.dto.response.AdminSchdListResponse;
import com.mycrewsoft.domain.schedule.dto.response.AdminSchdResponse;
import com.mycrewsoft.domain.schedule.service.AdminScheduleService;
import com.mycrewsoft.domain.schedule.vo.AdminSchdSearchVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Schedule", description = "관리자 일정 관리 API")
@RestController
@RequestMapping("/admin/schedules")
@RequiredArgsConstructor
public class AdminScheduleController {

    private final AdminScheduleService adminScheduleService;

    @Operation(summary = "관리자 일정 목록 조회", description = "분류 코드, 기간, 키워드로 필터링된 일정 목록을 페이징으로 반환합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminSchdListResponse>>> getSchdList(
            @ModelAttribute AdminSchdSearchVO search,
            @PageableDefault(size = 10, sort = "schdRegstDt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AdminSchdListResponse> page = adminScheduleService.getSchdList(search, pageable);

        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(summary = "관리자 일정 단건 조회")
    @GetMapping("/{schdId}")
    public ResponseEntity<ApiResponse<AdminSchdResponse>> getSchd(
            @PathVariable Long schdId) {

        AdminSchdResponse response = adminScheduleService.getSchd(schdId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "관리자 일정 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createSchd(
            @Valid @RequestBody AdminSchdRequest request) {

        Long schdId = adminScheduleService.createSchd(request);

        return ResponseEntity.ok(ApiResponse.success(schdId));
    }

    @Operation(summary = "관리자 일정 수정")
    @PutMapping("/{schdId}")
    public ResponseEntity<ApiResponse<Void>> modifySchd(
            @PathVariable Long schdId,
            @Valid @RequestBody AdminSchdRequest request) {

        adminScheduleService.modifySchd(schdId, request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "관리자 일정 삭제")
    @DeleteMapping("/{schdId}")
    public ResponseEntity<ApiResponse<Void>> deleteSchd(
            @PathVariable Long schdId) {

        adminScheduleService.deleteSchd(schdId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}