package com.mycrewsoft.domain.dashboard.service;

import com.mycrewsoft.domain.dashboard.dto.response.widget.AdminAttendanceWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AdminImportantScheduleWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AdminNoticeWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AdminProjectStatusWidgetResponse;

/**
 * 관리자 대시보드 위젯 서비스.
 *
 * 사용자 페이지 대시보드({@link DashboardWidgetService})와 달리 조직 전체 관점의 데이터를 제공한다.
 * 모든 조회는 ADMIN_CONSOLE_ACCESS(관리자 페이지 접속) 권한을 요구한다.
 */
public interface AdminDashboardWidgetService {

    /** 사원 근태 현황 위젯: 오늘 지각/조퇴/결근 등 특이사항 사원 목록(최대 5명) */
    AdminAttendanceWidgetResponse readAttendanceWidget();

    /** 중요 일정 위젯: 오늘 + 다가오는 전사·간부 일정 통합 */
    AdminImportantScheduleWidgetResponse readImportantScheduleWidget();

    /** 프로젝트 현황 위젯: 전체 프로젝트 상태별 집계 통계 */
    AdminProjectStatusWidgetResponse readProjectStatusWidget();

    /** 공지사항 위젯: 최신 공지 목록(최대 5개) */
    AdminNoticeWidgetResponse readNoticeWidget();
}
