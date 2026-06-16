package com.mycrewsoft.domain.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.ApprovalWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AttendanceWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.BoardWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.MailWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.MeetingWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.MessengerWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.NotificationWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.ProjectWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.ReservationWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.ScheduleWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.TaskWidgetResponse;
import com.mycrewsoft.domain.dashboard.service.DashboardWidgetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Dashboard Widget", description = "대시보드 위젯 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard/widgets")
public class DashboardWidgetController {

    private final DashboardWidgetService dashboardWidgetService;

    @Operation(summary = "근태 위젯")
    @GetMapping("/attendance")
    public ApiResponse<AttendanceWidgetResponse> readAttendanceWidget() {
        return ApiResponse.success(dashboardWidgetService.readAttendanceWidget());
    }

    @Operation(summary = "전자결재 위젯")
    @GetMapping("/approval")
    public ApiResponse<ApprovalWidgetResponse> readApprovalWidget() {
        return ApiResponse.success(dashboardWidgetService.readApprovalWidget());
    }

    @Operation(summary = "오늘 일정 위젯")
    @GetMapping("/schedule")
    public ApiResponse<ScheduleWidgetResponse> readScheduleWidget() {
        return ApiResponse.success(dashboardWidgetService.readScheduleWidget());
    }

    @Operation(summary = "오늘 회의 위젯")
    @GetMapping("/meeting")
    public ApiResponse<MeetingWidgetResponse> readMeetingWidget() {
        return ApiResponse.success(dashboardWidgetService.readMeetingWidget());
    }

    @Operation(summary = "오늘 회의실 예약 위젯")
    @GetMapping("/reservation")
    public ApiResponse<ReservationWidgetResponse> readReservationWidget() {
        return ApiResponse.success(dashboardWidgetService.readReservationWidget());
    }

    @Operation(summary = "업무 위젯")
    @GetMapping("/task")
    public ApiResponse<TaskWidgetResponse> readTaskWidget() {
        return ApiResponse.success(dashboardWidgetService.readTaskWidget());
    }

    @Operation(summary = "프로젝트 위젯")
    @GetMapping("/project")
    public ApiResponse<ProjectWidgetResponse> readProjectWidget() {
        return ApiResponse.success(dashboardWidgetService.readProjectWidget());
    }

    @Operation(summary = "게시판 위젯")
    @GetMapping("/board")
    public ApiResponse<BoardWidgetResponse> readBoardWidget(
            @RequestParam(defaultValue = "NOTICE") String boardTypeCd) {
        return ApiResponse.success(dashboardWidgetService.readBoardWidget(boardTypeCd));
    }

    @Operation(summary = "메신저 위젯")
    @GetMapping("/messenger")
    public ApiResponse<MessengerWidgetResponse> readMessengerWidget() {
        return ApiResponse.success(dashboardWidgetService.readMessengerWidget());
    }

    @Operation(summary = "알림 위젯")
    @GetMapping("/notification")
    public ApiResponse<NotificationWidgetResponse> readNotificationWidget() {
        return ApiResponse.success(dashboardWidgetService.readNotificationWidget());
    }
    
    @Operation(summary = "메일 위젯")
    @GetMapping("/mail")
    public ApiResponse<MailWidgetResponse> readMailWidget() {
        return ApiResponse.success(dashboardWidgetService.readMailWidget());
    }
}