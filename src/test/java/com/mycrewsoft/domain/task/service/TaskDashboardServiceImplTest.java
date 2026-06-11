package com.mycrewsoft.domain.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DateUtil;
import com.mycrewsoft.domain.projectmember.mapper.ProjectMemberMapper;
import com.mycrewsoft.domain.projectmember.vo.ProjectMemberVO;
import com.mycrewsoft.domain.task.dto.response.TaskDashboardResponse;
import com.mycrewsoft.domain.task.dto.response.TaskDashboardSummaryResponse;
import com.mycrewsoft.domain.task.dto.response.TaskUpcomingResponse;
import com.mycrewsoft.domain.task.mapper.TaskDashboardMapper;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("업무 대시보드 서비스 단위 테스트")
class TaskDashboardServiceImplTest {

    @InjectMocks
    private TaskDashboardServiceImpl taskDashboardService;

    @Mock private TaskDashboardMapper dashboardMapper;
    @Mock private ProjectMemberMapper projectMemberMapper;

    // ── 공통 픽스처 헬퍼 ──────────────────────────────────────────

    private ProjectMemberVO memberVO(Long empId) {
        ProjectMemberVO vo = new ProjectMemberVO();
        vo.setEmpId(empId);
        return vo;
    }

    private TaskUpcomingResponse upcomingVO(Long taskId, LocalDate endDt) {
        TaskUpcomingResponse vo = new TaskUpcomingResponse();
        vo.setTaskId(taskId);
        vo.setTaskEndDt(endDt);
        return vo;
    }

    // ─────────────────────────────────────────────────────────────
    //  GET TASK DASHBOARD
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("대시보드 조회 - 성공")
    void getTaskDashboard_success() {
        Long projId = 1L;
        Long empId = 100L;

        TaskDashboardSummaryResponse summary = new TaskDashboardSummaryResponse();
        summary.setTotalCount(10);
        summary.setCompletedCount(3);
        summary.setInProgressCount(5);
        summary.setStopCount(2);

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<TaskUpcomingResponse> upcomingTasks = List.of(upcomingVO(1L, tomorrow));

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class);
             MockedStatic<DateUtil> dateUtil = Mockito.mockStatic(DateUtil.class)) {

            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            dateUtil.when(() -> DateUtil.dDay(tomorrow)).thenReturn("D-1");

            given(projectMemberMapper.selectProjectMemberList(projId))
                    .willReturn(List.of(memberVO(empId)));
            given(dashboardMapper.selectTaskDashboardSummary(projId)).willReturn(summary);
            given(dashboardMapper.selectUpcomingTasks(projId)).willReturn(upcomingTasks);

            TaskDashboardResponse result = taskDashboardService.getTaskDashboard(projId);

            assertThat(result).isNotNull();
            assertThat(result.getSummary().getTotalCount()).isEqualTo(10);
            assertThat(result.getSummary().getCompletedCount()).isEqualTo(3);
            assertThat(result.getSummary().getInProgressCount()).isEqualTo(5);
            assertThat(result.getSummary().getStopCount()).isEqualTo(2);
            assertThat(result.getUpcomingTasks()).hasSize(1);
            assertThat(result.getUpcomingTasks().get(0).getDDay()).isEqualTo("D-1");
        }
    }

    @Test
    @DisplayName("대시보드 조회 - 마감 임박 업무 없을 때 빈 리스트 반환")
    void getTaskDashboard_emptyUpcomingTasks() {
        Long projId = 1L;
        Long empId = 100L;

        TaskDashboardSummaryResponse summary = new TaskDashboardSummaryResponse();

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(projectMemberMapper.selectProjectMemberList(projId))
                    .willReturn(List.of(memberVO(empId)));
            given(dashboardMapper.selectTaskDashboardSummary(projId)).willReturn(summary);
            given(dashboardMapper.selectUpcomingTasks(projId)).willReturn(List.of());

            TaskDashboardResponse result = taskDashboardService.getTaskDashboard(projId);

            assertThat(result.getUpcomingTasks()).isEmpty();
        }
    }

    @Test
    @DisplayName("대시보드 조회 - D-Day 당일 업무 dDay 확인")
    void getTaskDashboard_dDayToday() {
        Long projId = 1L;
        Long empId = 100L;

        TaskDashboardSummaryResponse summary = new TaskDashboardSummaryResponse();
        LocalDate today = LocalDate.now();
        List<TaskUpcomingResponse> upcomingTasks = List.of(upcomingVO(1L, today));

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class);
             MockedStatic<DateUtil> dateUtil = Mockito.mockStatic(DateUtil.class)) {

            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            dateUtil.when(() -> DateUtil.dDay(today)).thenReturn("D-Day");

            given(projectMemberMapper.selectProjectMemberList(projId))
                    .willReturn(List.of(memberVO(empId)));
            given(dashboardMapper.selectTaskDashboardSummary(projId)).willReturn(summary);
            given(dashboardMapper.selectUpcomingTasks(projId)).willReturn(upcomingTasks);

            TaskDashboardResponse result = taskDashboardService.getTaskDashboard(projId);

            assertThat(result.getUpcomingTasks().get(0).getDDay()).isEqualTo("D-Day");
        }
    }

    @Test
    @DisplayName("대시보드 조회 - 프로젝트 참여자 아닐 때 예외")
    void getTaskDashboard_notParticipant_fail() {
        Long projId = 1L;
        Long empId = 100L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(projectMemberMapper.selectProjectMemberList(projId))
                    .willReturn(List.of(memberVO(999L)));

            assertThatThrownBy(() -> taskDashboardService.getTaskDashboard(projId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_PARTICIPANT);

            verify(dashboardMapper, never()).selectTaskDashboardSummary(any());
            verify(dashboardMapper, never()).selectUpcomingTasks(any());
        }
    }

    @Test
    @DisplayName("대시보드 조회 - 비로그인 시 예외")
    void getTaskDashboard_unauthorized_fail() {
        Long projId = 1L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(null);

            assertThatThrownBy(() -> taskDashboardService.getTaskDashboard(projId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);

            verify(projectMemberMapper, never()).selectProjectMemberList(any());
            verify(dashboardMapper, never()).selectTaskDashboardSummary(any());
        }
    }
}