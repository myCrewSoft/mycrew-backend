package com.mycrewsoft.domain.dashboard.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.util.DateUtil;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDraftSummaryResponse;
import com.mycrewsoft.domain.approval.service.ApprovalSearchService;
import com.mycrewsoft.domain.attendance.dto.response.AtndTodayResponse;
import com.mycrewsoft.domain.attendance.service.AttendanceService;
import com.mycrewsoft.domain.board.dto.response.BoardWidgetItemResponse;
import com.mycrewsoft.domain.board.service.BoardService;
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
import com.mycrewsoft.domain.mail.dto.response.MailSummaryResponse;
import com.mycrewsoft.domain.mail.service.MailService;
import com.mycrewsoft.domain.messenger.dto.response.ChatRoomResponse;
import com.mycrewsoft.domain.messenger.service.MsngrService;
import com.mycrewsoft.domain.mtng.dto.response.MtngListResponse;
import com.mycrewsoft.domain.mtng.service.MtngService;
import com.mycrewsoft.domain.notification.dto.response.NotificationResponse;
import com.mycrewsoft.domain.notification.service.NotificationService;
import com.mycrewsoft.domain.project.dto.ProjectListResponseDto;
import com.mycrewsoft.domain.project.service.ProjectService;
import com.mycrewsoft.domain.reservation.dto.response.ReservationResponse;
import com.mycrewsoft.domain.reservation.service.ReservationService;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleWidgetItemResponse;
import com.mycrewsoft.domain.schedule.service.ScheduleService;
import com.mycrewsoft.domain.task.dto.response.TaskListResponse;
import com.mycrewsoft.domain.task.service.TaskService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardWidgetServiceImpl implements DashboardWidgetService {

    private final AttendanceService     attendanceService;
    private final ApprovalSearchService approvalSearchService;
    private final ScheduleService       scheduleService;
    private final MtngService           mtngService;
    private final ReservationService    reservationService;
    private final TaskService           taskService;
    private final ProjectService        projectService;
    private final BoardService          boardService;
    private final MsngrService          msngrService;
    private final NotificationService   notificationService;
    private final MailService 			mailService;
    
    @Override
    public AttendanceWidgetResponse readAttendanceWidget() {
        AtndTodayResponse today = attendanceService.getMyToday();

        return AttendanceWidgetResponse.builder()
                .status(today.getAtndStatCd())
                .checkInAt(today.getWrkStartDtm() != null
                        ? today.getWrkStartDtm().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                        : null)
                .checkOutAt(today.getWrkEndDtm() != null
                        ? today.getWrkEndDtm().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                        : null)
                .workDurationMinutes(today.getWorkMin())
                .build();
    }

    @Override
    public ApprovalWidgetResponse readApprovalWidget() {
        List<ApprovalDraftSummaryResponse> list = approvalSearchService.readPendingApprovalsForWidget();

        List<ApprovalWidgetResponse.ApprovalItem> items = list.stream()
                .map(dto -> ApprovalWidgetResponse.ApprovalItem.builder()
                        .id(dto.getDrftDocSn())
                        .title(dto.getDocTtl())
                        .requesterName(dto.getDrafterEmpNm())
                        .requestedAt(DateUtil.format(dto.getDrftReqstDt()))
                        .dueDate(dto.getAprvlHopeDt() != null
                                ? dto.getAprvlHopeDt().toLocalDate().toString()
                                : null)
                        .dDay(dto.getAprvlHopeDt() != null
                                ? DateUtil.dDay(dto.getAprvlHopeDt().toLocalDate())
                                : null)
                        .build())
                .toList();

        return ApprovalWidgetResponse.builder()
                .pendingCount(items.size())
                .documents(items)
                .build();
    }

    @Override
    public ScheduleWidgetResponse readScheduleWidget() {
        List<ScheduleWidgetItemResponse> list = scheduleService.readTodaySchdListForWidget();

        List<ScheduleWidgetResponse.ScheduleItem> items = list.stream()
                .map(dto -> ScheduleWidgetResponse.ScheduleItem.builder()
                        .id(dto.getId())
                        .title(dto.getTitle())
                        .scheduleTypeCode(dto.getScheduleTypeCode())
                        .startAt(DateUtil.formatTime(dto.getStart()))
                        .endAt(DateUtil.formatTime(dto.getEnd()))
                        .deptNm(dto.getDeptNm())
                        .projNm(dto.getProjNm())
                        .taskNm(dto.getTaskNm())
                        .build())
                .toList();

        return ScheduleWidgetResponse.builder()
                .schedules(items)
                .build();
    }

    @Override
    public MeetingWidgetResponse readMeetingWidget() {
        List<MtngListResponse> list = mtngService.getMtngListForWidget();

        List<MeetingWidgetResponse.MeetingItem> items = list.stream()
                .map(dto -> MeetingWidgetResponse.MeetingItem.builder()
                        .id(dto.getMtngId())
                        .title(dto.getMtngNm())
                        .startAt(dto.getBeginDt() != null
                                ? dto.getBeginDt().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                : null)
                        .endAt(dto.getEndDt() != null
                                ? dto.getEndDt().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                : null)
                        .location(dto.getConfRmNm())
                        .build())
                .toList();

        return MeetingWidgetResponse.builder()
                .meetings(items)
                .build();
    }

    @Override
    public ReservationWidgetResponse readReservationWidget() {
        List<ReservationResponse> list = reservationService.readReservationListForWidget();

        List<ReservationWidgetResponse.ReservationItem> items = list.stream()
                .map(dto -> ReservationWidgetResponse.ReservationItem.builder()
                        .id(dto.getReservationId())
                        .resourceName(dto.getTitle())
                        .startAt(dto.getStartDateTime() != null
                                ? dto.getStartDateTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                : null)
                        .endAt(dto.getEndDateTime() != null
                                ? dto.getEndDateTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                : null)
                        .status(dto.getAllDayYn())
                        .build())
                .toList();

        return ReservationWidgetResponse.builder()
                .reservations(items)
                .build();
    }

    @Override
    public TaskWidgetResponse readTaskWidget() {
        List<TaskListResponse> list = taskService.getTaskListForWidget();

        List<TaskWidgetResponse.TaskItem> items = list.stream()
                .map(dto -> TaskWidgetResponse.TaskItem.builder()
                        .id(dto.getTaskId())
                        .title(dto.getTaskNm())
                        .dueDate(dto.getTaskEndDt() != null
                                ? dto.getTaskEndDt().toLocalDate().toString()
                                : null)
                        .status(dto.getTaskStatCd())
                        .projId(dto.getProjId())
                        .build())
                .toList();

        return TaskWidgetResponse.builder()
                .tasks(items)
                .build();
    }

    @Override
    public ProjectWidgetResponse readProjectWidget() {
        List<ProjectListResponseDto> list = projectService.getProjectListForWidget();

        List<ProjectWidgetResponse.ProjectItem> items = list.stream()
                .map(dto -> ProjectWidgetResponse.ProjectItem.builder()
                        .id(dto.getProjId())
                        .name(dto.getProjNm())
                        .progressRate(dto.getProjPrgrsRt() != null ? dto.getProjPrgrsRt() : 0)
                        .dueDate(dto.getProjEndYmd() != null
                                ? dto.getProjEndYmd().toString()
                                : null)
                        .build())
                .toList();

        return ProjectWidgetResponse.builder()
                .projects(items)
                .build();
    }

    @Override
    public BoardWidgetResponse readBoardWidget(String boardTypeCd) {
        List<BoardWidgetItemResponse> list = boardService.getBoardListForWidget(boardTypeCd, 3);

        List<BoardWidgetResponse.BoardItem> items = list.stream()
                .map(dto -> BoardWidgetResponse.BoardItem.builder()
                        .id(dto.getId())
                        .title(dto.getTitle())
                        .writerName(dto.getWriterName())
                        .createdAt(dto.getCreatedAt() != null
                                ? dto.getCreatedAt().toString()
                                : null)
                        .build())
                .toList();

        return BoardWidgetResponse.builder()
                .posts(items)
                .build();
    }

    @Override
    public MessengerWidgetResponse readMessengerWidget() {
        List<ChatRoomResponse> list = msngrService.getChtrmListForWidget();

        int totalUnread = list.stream()
                .mapToInt(r -> r.getUnreadCount() != null ? r.getUnreadCount() : 0)
                .sum();

        List<MessengerWidgetResponse.MessengerItem> items = list.stream()
                .map(dto -> MessengerWidgetResponse.MessengerItem.builder()
                        .roomId(dto.getId())
                        .roomName(dto.getName())
                        .lastMessage(dto.getLastMessage())
                        .lastMessageAt(dto.getLastTime())
                        .unreadCount(dto.getUnreadCount() != null ? dto.getUnreadCount() : 0)
                        .build())
                .toList();

        return MessengerWidgetResponse.builder()
                .unreadCount(totalUnread)
                .rooms(items)
                .build();
    }

    @Override
    public NotificationWidgetResponse readNotificationWidget() {
        List<NotificationResponse> list = notificationService.readAlrmListForWidget();

        int unreadCount = (int) list.stream()
                .filter(n -> n.getAlrmCfmtnDt() == null)
                .count();

        List<NotificationWidgetResponse.NotificationItem> items = list.stream()
                .map(dto -> NotificationWidgetResponse.NotificationItem.builder()
                        .id(dto.getAlrmRcvrId())
                        .title(dto.getAlrmTtln())
                        .content(dto.getAlrmCn())
                        .createdAt(dto.getAlrmSndngDt() != null
                                ? dto.getAlrmSndngDt().toString()
                                : null)
                        .type(dto.getAlrmTypeCd())
                        .build())
                .toList();

        return NotificationWidgetResponse.builder()
                .count(unreadCount)
                .notifications(items)
                .build();
    }
    
    @Override
    public MailWidgetResponse readMailWidget() {
        List<MailSummaryResponse> list = mailService.getMailsForWidget();

        int unreadCount = (int) list.stream()
                .filter(MailSummaryResponse::isUnread)
                .count();

        List<MailWidgetResponse.MailItem> items = list.stream()
                .map(dto -> MailWidgetResponse.MailItem.builder()
                        .id(dto.getMailId())
                        .senderName(dto.getFromEmail())
                        .subject(dto.getSubject())
                        .receivedAt(DateUtil.format(dto.getSentAt()))
                        .isRead(!dto.isUnread())
                        .build())
                .toList();

        return MailWidgetResponse.builder()
                .unreadCount(unreadCount)
                .mails(items)
                .build();
    }
}