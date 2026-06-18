package com.mycrewsoft.domain.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AdminAttendanceWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AdminImportantScheduleWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AdminNoticeWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AdminProjectStatusWidgetResponse;
import com.mycrewsoft.domain.dashboard.service.AdminDashboardWidgetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 대시보드 위젯 API.
 *
 * 사용자 페이지 대시보드(/dashboard/widgets)와 분리된 관리자 전용 경로(/admin/dashboard/widgets)로
 * 조직 전체 관점의 위젯 데이터를 제공한다. 모든 엔드포인트는 ADMIN_CONSOLE_ACCESS 권한이 필요하다.
 */
@Tag(name = "Admin Dashboard Widget", description = "관리자 대시보드 위젯 API (조직 전체 현황)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/dashboard/widgets")
public class AdminDashboardWidgetController {

    private final AdminDashboardWidgetService adminDashboardWidgetService;

    @Operation(summary = "사원 근태 현황 위젯",
            description = "오늘 지각·조퇴·결근 등 특이사항이 있는 사원 목록(최대 5명)을 조회합니다. ADMIN_CONSOLE_ACCESS 권한이 필요합니다.")
    @GetMapping("/attendance")
    public ApiResponse<AdminAttendanceWidgetResponse> readAttendanceWidget() {
        return ApiResponse.success(adminDashboardWidgetService.readAttendanceWidget());
    }

    @Operation(summary = "중요 일정 위젯",
            description = "오늘 및 다가오는 전사·간부 일정을 통합하여 조회합니다. ADMIN_CONSOLE_ACCESS 권한이 필요합니다.")
    @GetMapping("/schedule")
    public ApiResponse<AdminImportantScheduleWidgetResponse> readImportantScheduleWidget() {
        return ApiResponse.success(adminDashboardWidgetService.readImportantScheduleWidget());
    }

    @Operation(summary = "프로젝트 현황 위젯",
            description = "전체 프로젝트의 상태별(예정/진행 중/완료/중단) 집계 통계를 조회합니다. ADMIN_CONSOLE_ACCESS 권한이 필요합니다.")
    @GetMapping("/project")
    public ApiResponse<AdminProjectStatusWidgetResponse> readProjectStatusWidget() {
        return ApiResponse.success(adminDashboardWidgetService.readProjectStatusWidget());
    }

    @Operation(summary = "공지사항 위젯",
            description = "현재 등록된 공지사항 중 최신 5개를 조회합니다. ADMIN_CONSOLE_ACCESS 권한이 필요합니다.")
    @GetMapping("/notice")
    public ApiResponse<AdminNoticeWidgetResponse> readNoticeWidget() {
        return ApiResponse.success(adminDashboardWidgetService.readNoticeWidget());
    }
}
