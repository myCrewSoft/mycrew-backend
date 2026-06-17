package com.mycrewsoft.domain.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.domain.approval.dto.response.ApprovalDraftSummaryResponse;
import com.mycrewsoft.domain.approval.service.ApprovalSearchService;
import com.mycrewsoft.domain.attendance.dto.response.AtndTodayResponse;
import com.mycrewsoft.domain.attendance.service.AttendanceService;
import com.mycrewsoft.domain.board.dto.response.BoardWidgetItemResponse;
import com.mycrewsoft.domain.board.service.BoardService;
import com.mycrewsoft.domain.dashboard.dto.response.widget.ApprovalWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.AttendanceWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.BoardWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.MessengerWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.NotificationWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.ProjectWidgetResponse;
import com.mycrewsoft.domain.dashboard.dto.response.widget.ScheduleWidgetResponse;
import com.mycrewsoft.domain.messenger.dto.response.ChatRoomResponse;
import com.mycrewsoft.domain.messenger.service.MsngrService;
import com.mycrewsoft.domain.mtng.service.MtngService;
import com.mycrewsoft.domain.notification.dto.response.NotificationResponse;
import com.mycrewsoft.domain.notification.service.NotificationService;
import com.mycrewsoft.domain.project.dto.ProjectListResponseDto;
import com.mycrewsoft.domain.project.service.ProjectService;
import com.mycrewsoft.domain.reservation.service.ReservationService;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleWidgetItemResponse;
import com.mycrewsoft.domain.schedule.service.ScheduleService;
import com.mycrewsoft.domain.task.service.TaskService;

@ExtendWith(MockitoExtension.class)
class DashboardWidgetServiceImplTest {

    @InjectMocks
    private DashboardWidgetServiceImpl dashboardWidgetService;

    @Mock private AttendanceService     attendanceService;
    @Mock private ApprovalSearchService approvalSearchService;
    @Mock private ScheduleService       scheduleService;
    @Mock private MtngService           mtngService;
    @Mock private ReservationService    reservationService;
    @Mock private TaskService           taskService;
    @Mock private ProjectService        projectService;
    @Mock private BoardService          boardService;
    @Mock private MsngrService          msngrService;
    @Mock private NotificationService   notificationService;

    @Test
    @DisplayName("근태 위젯 - 출근 상태 반환")
    void readAttendanceWidget_working() {
        AtndTodayResponse today = AtndTodayResponse.builder()
                .atndStatCd("WORKING")
                .wrkStartDtm(LocalDateTime.of(2026, 6, 16, 9, 0))
                .wrkEndDtm(null)
                .workMin(120)
                .build();

        given(attendanceService.getMyToday()).willReturn(today);

        AttendanceWidgetResponse result = dashboardWidgetService.readAttendanceWidget();

        assertThat(result.getStatus()).isEqualTo("WORKING");
        assertThat(result.getCheckInAt()).isEqualTo("09:00");
        assertThat(result.getCheckOutAt()).isNull();
        assertThat(result.getWorkDurationMinutes()).isEqualTo(120);
    }

    @Test
    @DisplayName("근태 위젯 - 미출근 상태 반환")
    void readAttendanceWidget_notCheckedIn() {
        AtndTodayResponse today = AtndTodayResponse.builder()
                .atndStatCd(null)
                .wrkStartDtm(null)
                .wrkEndDtm(null)
                .workMin(0)
                .build();

        given(attendanceService.getMyToday()).willReturn(today);

        AttendanceWidgetResponse result = dashboardWidgetService.readAttendanceWidget();

        assertThat(result.getCheckInAt()).isNull();
        assertThat(result.getWorkDurationMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("전자결재 위젯 - 결재 대기 목록 반환 (기안일시, 희망일시, D-day 포함)")
    void readApprovalWidget_pendingList() {
        LocalDateTime drftReqstDt = LocalDateTime.of(2026, 6, 10, 9, 0);
        LocalDateTime aprvlHopeDt = LocalDate.now().plusDays(3).atTime(18, 0);

        ApprovalDraftSummaryResponse doc = new ApprovalDraftSummaryResponse();
        setField(doc, "drftDocSn", 1L);
        setField(doc, "docTtl", "휴가 신청서");
        setField(doc, "drafterEmpNm", "홍길동");
        setField(doc, "drftReqstDt", drftReqstDt);
        setField(doc, "aprvlHopeDt", aprvlHopeDt);

        given(approvalSearchService.readPendingApprovalsForWidget()).willReturn(List.of(doc));

        ApprovalWidgetResponse result = dashboardWidgetService.readApprovalWidget();

        assertThat(result.getPendingCount()).isEqualTo(1);
        assertThat(result.getDocuments()).hasSize(1);

        ApprovalWidgetResponse.ApprovalItem item = result.getDocuments().get(0);
        assertThat(item.getTitle()).isEqualTo("휴가 신청서");
        assertThat(item.getRequesterName()).isEqualTo("홍길동");
        assertThat(item.getRequestedAt()).isEqualTo("2026-06-10 09:00:00");
        assertThat(item.getDueDate()).isEqualTo(aprvlHopeDt.toLocalDate().toString());
        assertThat(item.getDDay()).isEqualTo("D-3");
    }

    @Test
    @DisplayName("전자결재 위젯 - 희망일시 없으면 D-day null 반환")
    void readApprovalWidget_noDueDate() {
        ApprovalDraftSummaryResponse doc = new ApprovalDraftSummaryResponse();
        setField(doc, "drftDocSn", 2L);
        setField(doc, "docTtl", "구매 품의서");
        setField(doc, "drafterEmpNm", "김철수");
        setField(doc, "drftReqstDt", LocalDateTime.of(2026, 6, 15, 10, 0));
        setField(doc, "aprvlHopeDt", null);

        given(approvalSearchService.readPendingApprovalsForWidget()).willReturn(List.of(doc));

        ApprovalWidgetResponse result = dashboardWidgetService.readApprovalWidget();

        ApprovalWidgetResponse.ApprovalItem item = result.getDocuments().get(0);
        assertThat(item.getDueDate()).isNull();
        assertThat(item.getDDay()).isNull();
    }

    @Test
    @DisplayName("전자결재 위젯 - 결재 대기 없으면 빈 목록 반환")
    void readApprovalWidget_emptyList() {
        given(approvalSearchService.readPendingApprovalsForWidget()).willReturn(List.of());

        ApprovalWidgetResponse result = dashboardWidgetService.readApprovalWidget();

        assertThat(result.getPendingCount()).isEqualTo(0);
        assertThat(result.getDocuments()).isEmpty();
    }

    @Test
    @DisplayName("오늘 일정 위젯 - 일정 목록 반환")
    void readScheduleWidget_scheduleList() {
        ScheduleWidgetItemResponse item = new ScheduleWidgetItemResponse();
        setField(item, "id", 1L);
        setField(item, "title", "팀 회의");
        setField(item, "start", LocalDateTime.of(2026, 6, 16, 10, 0));
        setField(item, "end", LocalDateTime.of(2026, 6, 16, 11, 0));
        setField(item, "scheduleTypeCode", "C004");
        setField(item, "deptNm", "개발팀");

        given(scheduleService.readTodaySchdListForWidget()).willReturn(List.of(item));

        ScheduleWidgetResponse result = dashboardWidgetService.readScheduleWidget();

        assertThat(result.getSchedules()).hasSize(1);
        assertThat(result.getSchedules().get(0).getTitle()).isEqualTo("팀 회의");
        assertThat(result.getSchedules().get(0).getStartAt()).isEqualTo("10:00");
        assertThat(result.getSchedules().get(0).getDeptNm()).isEqualTo("개발팀");
    }

    @Test
    @DisplayName("프로젝트 위젯 - 진행 중인 프로젝트 목록 반환")
    void readProjectWidget_projectList() {
        ProjectListResponseDto proj = new ProjectListResponseDto();
        setField(proj, "projId", 1L);
        setField(proj, "projNm", "그룹웨어 고도화");
        setField(proj, "projPrgrsRt", 72);
        setField(proj, "projEndYmd", LocalDate.of(2026, 12, 31));

        given(projectService.getProjectListForWidget()).willReturn(List.of(proj));

        ProjectWidgetResponse result = dashboardWidgetService.readProjectWidget();

        assertThat(result.getProjects()).hasSize(1);
        assertThat(result.getProjects().get(0).getName()).isEqualTo("그룹웨어 고도화");
        assertThat(result.getProjects().get(0).getProgressRate()).isEqualTo(72);
    }

    @Test
    @DisplayName("알림 위젯 - 미읽은 알림 수 계산")
    void readNotificationWidget_unreadCount() {
        NotificationResponse read = NotificationResponse.builder()
                .alrmRcvrId(1L)
                .alrmId(1L)
                .alrmTtln("읽은 알림")
                .alrmCn("내용")
                .alrmSndngDt(LocalDateTime.now())
                .alrmCfmtnDt(LocalDateTime.now())
                .build();

        NotificationResponse unread = NotificationResponse.builder()
                .alrmRcvrId(2L)
                .alrmId(2L)
                .alrmTtln("안 읽은 알림")
                .alrmCn("내용")
                .alrmSndngDt(LocalDateTime.now())
                .alrmCfmtnDt(null)
                .build();

        given(notificationService.readAlrmListForWidget()).willReturn(List.of(read, unread));

        NotificationWidgetResponse result = dashboardWidgetService.readNotificationWidget();

        assertThat(result.getCount()).isEqualTo(1);
        assertThat(result.getNotifications()).hasSize(2);
    }

    @Test
    @DisplayName("메신저 위젯 - 전체 미읽은 메시지 수 합산")
    void readMessengerWidget_totalUnreadCount() {
        ChatRoomResponse room1 = ChatRoomResponse.builder()
                .id(1L)
                .name("개발팀")
                .unreadCount(3)
                .build();

        ChatRoomResponse room2 = ChatRoomResponse.builder()
                .id(2L)
                .name("프로젝트 TF")
                .unreadCount(2)
                .build();

        given(msngrService.getChtrmListForWidget()).willReturn(List.of(room1, room2));

        MessengerWidgetResponse result = dashboardWidgetService.readMessengerWidget();

        assertThat(result.getUnreadCount()).isEqualTo(5);
        assertThat(result.getRooms()).hasSize(2);
    }

    @Test
    @DisplayName("게시판 위젯 - 공지 탭 게시글 반환")
    void readBoardWidget_noticeTab() {
        BoardWidgetItemResponse item = new BoardWidgetItemResponse();
        setField(item, "id", 1L);
        setField(item, "title", "6월 전사 공지");
        setField(item, "writerName", "관리자");
        setField(item, "createdAt", LocalDateTime.now());

        given(boardService.getBoardListForWidget("NOTICE", 3)).willReturn(List.of(item));

        BoardWidgetResponse result = dashboardWidgetService.readBoardWidget("NOTICE");

        assertThat(result.getPosts()).hasSize(1);
        assertThat(result.getPosts().get(0).getTitle()).isEqualTo("6월 전사 공지");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
