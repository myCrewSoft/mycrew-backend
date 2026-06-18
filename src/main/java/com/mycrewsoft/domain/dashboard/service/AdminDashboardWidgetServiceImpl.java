package com.mycrewsoft.domain.dashboard.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.attendance.dto.response.AdminAtndRowResponse;
import com.mycrewsoft.domain.attendance.service.AttendanceService;
import com.mycrewsoft.domain.board.dto.response.BoardWidgetItemResponse;
import com.mycrewsoft.domain.board.service.BoardService;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AdminAttendanceWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AdminImportantScheduleWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AdminNoticeWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AdminProjectStatusWidgetResponse;
import com.mycrewsoft.domain.project.service.ProjectService;
import com.mycrewsoft.domain.project.vo.ProjectStatusCountVO;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleWidgetItemResponse;
import com.mycrewsoft.domain.schedule.service.ScheduleService;
import com.mycrewsoft.security.authz.AuthorizationService;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 대시보드 위젯 서비스 구현체.
 *
 * 각 위젯 데이터는 도메인 서비스(근태/일정/프로젝트/게시판)를 재사용해 조립한다.
 * 모든 조회는 진입 시 ADMIN_CONSOLE_ACCESS 권한을 검증한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardWidgetServiceImpl implements AdminDashboardWidgetService {

    private static final int ATTENDANCE_ANOMALY_LIMIT = 5;
    private static final int IMPORTANT_SCHEDULE_LIMIT  = 5;
    private static final int NOTICE_LIMIT              = 5;
    private static final String NOTICE_BOARD_TYPE_CD   = "NOTICE";

    private static final DateTimeFormatter TIME_FORMAT      = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AuthorizationService authorizationService;
    private final AttendanceService    attendanceService;
    private final ScheduleService      scheduleService;
    private final ProjectService       projectService;
    private final BoardService         boardService;

    @Override
    public AdminAttendanceWidgetResponse readAttendanceWidget() {
        assertAdminAccess();

        List<AdminAtndRowResponse> rows =
                attendanceService.getAttendanceAnomaliesForWidget(ATTENDANCE_ANOMALY_LIMIT);

        List<AdminAttendanceWidgetResponse.AttendanceItem> items = rows.stream()
                .map(row -> AdminAttendanceWidgetResponse.AttendanceItem.builder()
                        .empId(row.getEmpId())
                        .empNm(row.getEmpNm())
                        .deptNm(row.getDeptNm())
                        .jbpsNm(row.getJbpsNm())
                        .status(row.getAtndStatCd())
                        .statusName(row.getAtndStatNm())
                        .checkInAt(formatTime(row.getWrkStartDtm()))
                        .checkOutAt(formatTime(row.getWrkEndDtm()))
                        .lateMin(row.getLateMin())
                        .earlyLeaveMin(row.getEarlyLeaveMin())
                        .build())
                .toList();

        return AdminAttendanceWidgetResponse.builder()
                .count(items.size())
                .employees(items)
                .build();
    }

    @Override
    public AdminImportantScheduleWidgetResponse readImportantScheduleWidget() {
        assertAdminAccess();

        List<ScheduleWidgetItemResponse> list =
                scheduleService.readImportantSchdListForAdminWidget(IMPORTANT_SCHEDULE_LIMIT);

        List<AdminImportantScheduleWidgetResponse.ScheduleItem> items = list.stream()
                .map(dto -> AdminImportantScheduleWidgetResponse.ScheduleItem.builder()
                        .id(dto.getId())
                        .title(dto.getTitle())
                        .scheduleTypeCode(dto.getScheduleTypeCode())
                        .scheduleTypeName(scheduleTypeName(dto.getScheduleTypeCode()))
                        .startAt(formatDateTime(dto.getStart()))
                        .endAt(formatDateTime(dto.getEnd()))
                        .allDay(Boolean.TRUE.equals(dto.getAllDay()))
                        .build())
                .toList();

        return AdminImportantScheduleWidgetResponse.builder()
                .schedules(items)
                .build();
    }

    @Override
    public AdminProjectStatusWidgetResponse readProjectStatusWidget() {
        assertAdminAccess();

        ProjectStatusCountVO counts = projectService.getProjectStatusCountsForWidget();

        List<AdminProjectStatusWidgetResponse.StatusCount> statusCounts = List.of(
                statusCount("01", "예정",   counts.getPlannedCount()),
                statusCount("02", "진행 중", counts.getInProgressCount()),
                statusCount("03", "완료",   counts.getCompletedCount()),
                statusCount("04", "중단",   counts.getStoppedCount())
        );

        return AdminProjectStatusWidgetResponse.builder()
                .totalCount(counts.getTotalCount())
                .statusCounts(statusCounts)
                .build();
    }

    @Override
    public AdminNoticeWidgetResponse readNoticeWidget() {
        assertAdminAccess();

        List<BoardWidgetItemResponse> list =
                boardService.getBoardListForWidget(NOTICE_BOARD_TYPE_CD, NOTICE_LIMIT);

        List<AdminNoticeWidgetResponse.NoticeItem> items = list.stream()
                .map(dto -> AdminNoticeWidgetResponse.NoticeItem.builder()
                        .id(dto.getId())
                        .title(dto.getTitle())
                        .writerName(dto.getWriterName())
                        .createdAt(formatDateTime(dto.getCreatedAt()))
                        .build())
                .toList();

        return AdminNoticeWidgetResponse.builder()
                .notices(items)
                .build();
    }

    // ============================ 내부 헬퍼 ============================

    /** 관리자 페이지 접속 권한(ADMIN_CONSOLE_ACCESS) 검증. 미보유 시 ACCESS_DENIED. */
    private void assertAdminAccess() {
        if (!authorizationService.hasGlobalScope(PermissionCode.ADMIN_CONSOLE_ACCESS)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    private AdminProjectStatusWidgetResponse.StatusCount statusCount(String code, String name, int count) {
        return AdminProjectStatusWidgetResponse.StatusCount.builder()
                .statusCode(code)
                .statusName(name)
                .count(count)
                .build();
    }

    private String scheduleTypeName(String scheduleTypeCode) {
        if (scheduleTypeCode == null) {
            return null;
        }
        return switch (scheduleTypeCode) {
            case "C001" -> "전사";
            case "C003" -> "간부";
            default -> scheduleTypeCode;
        };
    }

    private String formatTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_FORMAT) : null;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMAT) : null;
    }
}
