package com.mycrewsoft.domain.dashboard.controller;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.dashboard.dto.request.DashboardLayoutRequest;
import com.mycrewsoft.domain.dashboard.dto.response.DashboardLayoutResponse;
import com.mycrewsoft.domain.dashboard.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "대시보드 레이아웃 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "대시보드 레이아웃 조회")
    @GetMapping("/layout")
    public ApiResponse<DashboardLayoutResponse> readDashboardLayout() {
        return ApiResponse.success(dashboardService.readDashboardLayout());
    }

    @Operation(summary = "대시보드 레이아웃 저장")
    @PutMapping("/layout")
    public ApiResponse<Void> saveDashboardLayout(@RequestBody @Valid DashboardLayoutRequest request) {
        dashboardService.saveDashboardLayout(request);
        return ApiResponse.success();
    }
}