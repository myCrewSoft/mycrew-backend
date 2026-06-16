package com.mycrewsoft.domain.dashboard.service;

import com.mycrewsoft.domain.dashboard.dto.response.widget.ApprovalWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AttendanceWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.BoardWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.MeetingWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.MessengerWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.NotificationWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.ProjectWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.ReservationWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.ScheduleWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.TaskWidgetResponse;

public interface DashboardWidgetService {

    AttendanceWidgetResponse readAttendanceWidget();

    ApprovalWidgetResponse readApprovalWidget();

    ScheduleWidgetResponse readScheduleWidget();

    MeetingWidgetResponse readMeetingWidget();

    ReservationWidgetResponse readReservationWidget();

    TaskWidgetResponse readTaskWidget();

    ProjectWidgetResponse readProjectWidget();

    BoardWidgetResponse readBoardWidget(String boardTypeCd);

    MessengerWidgetResponse readMessengerWidget();

    NotificationWidgetResponse readNotificationWidget();
}