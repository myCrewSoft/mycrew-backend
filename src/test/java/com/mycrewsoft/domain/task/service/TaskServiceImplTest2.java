package com.mycrewsoft.domain.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.project.mapper.ProjectMapper;
import com.mycrewsoft.domain.projectmember.mapper.ProjectMemberMapper;
import com.mycrewsoft.domain.projectmember.vo.ProjectMemberVO;
import com.mycrewsoft.domain.task.dto.request.TaskCreateRequest;
import com.mycrewsoft.domain.task.dto.request.TaskUpdateRequest;
import com.mycrewsoft.domain.task.event.TaskAssignedEvent;
import com.mycrewsoft.domain.task.event.TaskCancelledEvent;
import com.mycrewsoft.domain.task.event.TaskManagerChangedEvent;
import com.mycrewsoft.domain.task.event.TaskMemberRemovedEvent;
import com.mycrewsoft.domain.task.event.TaskStatusChangedEvent;
import com.mycrewsoft.domain.task.mapper.TaskDtoMapper;
import com.mycrewsoft.domain.task.mapper.TaskMapper;
import com.mycrewsoft.domain.task.vo.TaskDetailVO;
import com.mycrewsoft.domain.task.vo.TaskPtcptDetailVO;
import com.mycrewsoft.domain.task.vo.TaskPtcptVO;
import com.mycrewsoft.domain.task.vo.TaskVO;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("업무 서비스 단위 테스트")
class TaskServiceImplTest2 {

    @InjectMocks
    private TaskServiceImpl taskService;

    @Mock private TaskMapper taskMapper;
    @Mock private TaskDtoMapper taskDtoMapper;
    @Mock private ProjectMemberMapper projectMemberMapper;
    @Mock private ProjectMapper projectMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    private static final Long CURRENT_EMP_ID = 1L;
    private static final Long PROJ_ID = 10L;
    private static final Long TASK_ID = 100L;

    // ── 헬퍼 메서드 ───────────────────────────────────────────────

    private TaskDetailVO makeTaskDetailVO(Long mngrId) {
        TaskDetailVO vo = new TaskDetailVO();
        vo.setTaskId(TASK_ID);
        vo.setTaskNm("테스트 업무");
        vo.setTaskMngrId(mngrId);
        vo.setTaskMngrNm("홍길동");
        vo.setTaskStatCd("00");

        TaskPtcptDetailVO ptcpt = new TaskPtcptDetailVO();
        ptcpt.setEmpId(2L);
        vo.setPtcptList(List.of(ptcpt));
        return vo;
    }

    private void mockCurrentUser() {
        // SecurityUtil은 static 메서드라 MockedStatic으로 감싸야 하는데
        // 헬퍼로 분리하면 try-with-resources 범위를 벗어나므로
        // 각 테스트에서 직접 선언합니다.
    }

    private void mockProjectMember(boolean isMember) {
        ProjectMemberVO memberVO = new ProjectMemberVO();
        memberVO.setEmpId(isMember ? CURRENT_EMP_ID : 999L);
        given(projectMemberMapper.selectProjectMemberList(PROJ_ID))
                .willReturn(List.of(memberVO));
    }

    // ── createTask ────────────────────────────────────────────────

    @Test
    @DisplayName("업무 생성 성공 - TaskAssignedEvent 발행")
    void createTask_success_publishTaskAssignedEvent() {
        TaskCreateRequest request = makeCreateRequest(List.of(2L, 3L));

        TaskVO taskVO = new TaskVO();
        taskVO.setTaskNm("테스트 업무");

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(CURRENT_EMP_ID);
            mockProjectMember(true);
            given(taskDtoMapper.toVO(any(TaskCreateRequest.class))).willReturn(taskVO);
            given(projectMapper.updateProjectPrgrsRtd(PROJ_ID)).willReturn(1);

            // insertTask 호출 시 taskId를 채워주는 Mock
            Mockito.doAnswer(invocation -> {
                TaskVO vo = invocation.getArgument(0);
                vo.setTaskId(TASK_ID);
                return null;
            }).when(taskMapper).insertTask(taskVO);

            given(taskDtoMapper.toPtcptVO(anyLong(), anyLong())).willReturn(new TaskPtcptVO());

            taskService.createTask(PROJ_ID, request);

            verify(taskMapper, times(1)).insertTask(taskVO);
            assertThat(taskVO.getTaskStatCd()).isEqualTo("01");
            verify(taskMapper, times(2)).insertTaskPtcpt(any());
            verify(eventPublisher, times(1)).publishEvent(any(TaskAssignedEvent.class));
        }
    }

    @Test
    @DisplayName("업무 생성 - 참여자 없으면 TaskAssignedEvent 미발행")
    void createTask_noParticipants_notPublishEvent() {
        TaskCreateRequest request = makeCreateRequest(null);

        TaskVO taskVO = new TaskVO();
        taskVO.setTaskNm("테스트 업무");

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(CURRENT_EMP_ID);
            mockProjectMember(true);
            given(taskDtoMapper.toVO(request)).willReturn(taskVO);
            given(projectMapper.updateProjectPrgrsRtd(PROJ_ID)).willReturn(1);

            taskService.createTask(PROJ_ID, request);

            verify(eventPublisher, times(0)).publishEvent(any(TaskAssignedEvent.class));
        }
    }

    @Test
    @DisplayName("업무 생성 실패 - 프로젝트 참여자 아님")
    void createTask_fail_notProjectParticipant() {
        TaskCreateRequest request = new TaskCreateRequest();

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(CURRENT_EMP_ID);
            mockProjectMember(false);

            assertThatThrownBy(() -> taskService.createTask(PROJ_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_PARTICIPANT);
        }
    }

    // ── updateTask ────────────────────────────────────────────────

    @Test
    @DisplayName("업무 수정 성공 - TaskStatusChangedEvent 발행")
    void updateTask_success_publishTaskStatusChangedEvent() {

        // existing.taskStatCd = "00" 이므로 request는 다른 값으로 세팅
        TaskUpdateRequest request = makeUpdateRequest("01");  // "00" 과 다른 값
        TaskVO taskVO = new TaskVO();
        TaskDetailVO existing = makeTaskDetailVO(CURRENT_EMP_ID);  // taskStatCd = "00"

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(CURRENT_EMP_ID);
            mockProjectMember(true);
            given(taskMapper.selectTaskDetail(TASK_ID)).willReturn(existing);
            given(taskDtoMapper.toVO(any(TaskUpdateRequest.class))).willReturn(taskVO);
            given(projectMapper.updateProjectPrgrsRtd(PROJ_ID)).willReturn(1);

            taskService.updateTask(PROJ_ID, TASK_ID, request);

            verify(taskMapper, times(1)).updateTask(TASK_ID, taskVO);
            verify(eventPublisher, times(1)).publishEvent(any(TaskStatusChangedEvent.class));
        }
    }

    @Test
    @DisplayName("업무 수정 실패 - 존재하지 않는 업무")
    void updateTask_fail_taskNotFound() {
        TaskUpdateRequest request = new TaskUpdateRequest();

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(CURRENT_EMP_ID);
            mockProjectMember(true);
            given(taskMapper.selectTaskDetail(TASK_ID)).willReturn(null);

            assertThatThrownBy(() -> taskService.updateTask(PROJ_ID, TASK_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_FOUND);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"02", "04"})
    @DisplayName("완료 또는 중지 상태인 업무는 수정할 수 없다")
    void updateTask_fail_nonEditableStatus(String taskStatCd) {
        TaskUpdateRequest request = makeUpdateRequest("01");
        TaskDetailVO existing = makeTaskDetailVO(CURRENT_EMP_ID);
        existing.setTaskStatCd(taskStatCd);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(CURRENT_EMP_ID);
            mockProjectMember(true);
            given(taskMapper.selectTaskDetail(TASK_ID)).willReturn(existing);

            assertThatThrownBy(() -> taskService.updateTask(PROJ_ID, TASK_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_INVALID_STAT_TRANSITION);

            verify(taskMapper, times(0)).updateTask(anyLong(), any());
        }
    }

    @Test
    @DisplayName("허용되지 않은 상태 코드로 업무를 수정할 수 없다")
    void updateTask_fail_invalidStatusCode() {
        TaskUpdateRequest request = makeUpdateRequest("05");
        TaskDetailVO existing = makeTaskDetailVO(CURRENT_EMP_ID);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(CURRENT_EMP_ID);
            mockProjectMember(true);
            given(taskMapper.selectTaskDetail(TASK_ID)).willReturn(existing);

            assertThatThrownBy(() -> taskService.updateTask(PROJ_ID, TASK_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_INVALID_STAT_TRANSITION);

            verify(taskMapper, times(0)).updateTask(anyLong(), any());
        }
    }

    // ── updateTaskManager ─────────────────────────────────────────

    @Test
    @DisplayName("업무 담당자 변경 성공 - TaskManagerChangedEvent 발행")
    void updateTaskManager_success_publishEvent() {
        Long newMngrId = 5L;
        TaskDetailVO existing = makeTaskDetailVO(CURRENT_EMP_ID);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(CURRENT_EMP_ID);
            given(taskMapper.selectTaskDetail(TASK_ID)).willReturn(existing);
            given(projectMapper.selectProjectLeaderId(PROJ_ID)).willReturn(CURRENT_EMP_ID);

            taskService.updateTaskManager(PROJ_ID, TASK_ID, newMngrId);

            verify(taskMapper, times(1)).updateTaskManager(TASK_ID, newMngrId, CURRENT_EMP_ID);
            verify(eventPublisher, times(1)).publishEvent(any(TaskManagerChangedEvent.class));
        }
    }

    // ── addTaskPtcpts ─────────────────────────────────────────────

    @Test
    @DisplayName("업무 참여자 추가 성공 - TaskAssignedEvent 발행")
    void addTaskPtcpts_success_publishEvent() {
        List<Long> empIdList = List.of(2L, 3L);
        TaskDetailVO existing = makeTaskDetailVO(CURRENT_EMP_ID);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(CURRENT_EMP_ID);
            given(taskMapper.selectTaskDetail(TASK_ID)).willReturn(existing);
            given(projectMapper.selectProjectLeaderId(PROJ_ID)).willReturn(CURRENT_EMP_ID);
            given(taskDtoMapper.toPtcptVO(anyLong(), anyLong())).willReturn(new TaskPtcptVO());

            taskService.addTaskPtcpts(PROJ_ID, TASK_ID, empIdList);

            verify(taskMapper, times(2)).insertTaskPtcpt(any());
            verify(eventPublisher, times(1)).publishEvent(any(TaskAssignedEvent.class));
        }
    }

    // ── removeTaskPtcpts ──────────────────────────────────────────

    @Test
    @DisplayName("업무 참여자 제거 성공 - TaskMemberRemovedEvent 발행")
    void removeTaskPtcpts_success_publishEvent() {
        List<Long> empIdList = List.of(2L);
        TaskDetailVO existing = makeTaskDetailVO(CURRENT_EMP_ID);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(CURRENT_EMP_ID);
            given(taskMapper.selectTaskDetail(TASK_ID)).willReturn(existing);
            given(projectMapper.selectProjectLeaderId(PROJ_ID)).willReturn(CURRENT_EMP_ID);
            given(taskMapper.deleteTaskPtcpt(TASK_ID, 2L)).willReturn(1);

            taskService.removeTaskPtcpts(PROJ_ID, TASK_ID, empIdList);

            verify(eventPublisher, times(1)).publishEvent(any(TaskMemberRemovedEvent.class));
        }
    }

    @Test
    @DisplayName("업무 참여자 제거 실패 - 존재하지 않는 참여자")
    void removeTaskPtcpts_fail_notParticipant() {
        List<Long> empIdList = List.of(2L);
        TaskDetailVO existing = makeTaskDetailVO(CURRENT_EMP_ID);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(CURRENT_EMP_ID);
            given(taskMapper.selectTaskDetail(TASK_ID)).willReturn(existing);
            given(projectMapper.selectProjectLeaderId(PROJ_ID)).willReturn(CURRENT_EMP_ID);
            given(taskMapper.deleteTaskPtcpt(TASK_ID, 2L)).willReturn(0);

            assertThatThrownBy(() -> taskService.removeTaskPtcpts(PROJ_ID, TASK_ID, empIdList))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_PARTICIPANT);
        }
    }

    // ── deleteTask ────────────────────────────────────────────────

    @Test
    @DisplayName("업무 삭제 성공 - TaskCancelledEvent 발행")
    void deleteTask_success_publishTaskCancelledEvent() {
        TaskDetailVO existing = makeTaskDetailVO(CURRENT_EMP_ID);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(CURRENT_EMP_ID);
            given(taskMapper.selectTaskDetail(TASK_ID)).willReturn(existing);
            given(projectMapper.selectProjectLeaderId(PROJ_ID)).willReturn(CURRENT_EMP_ID);
            given(projectMapper.updateProjectPrgrsRtd(PROJ_ID)).willReturn(1);

            taskService.deleteTask(PROJ_ID, TASK_ID);

            verify(taskMapper, times(1)).deleteTaskPtcpts(TASK_ID);
            verify(taskMapper, times(1)).deleteTask(TASK_ID, CURRENT_EMP_ID);
            verify(eventPublisher, times(1)).publishEvent(any(TaskCancelledEvent.class));
        }
    }

    @Test
    @DisplayName("업무 삭제 실패 - 권한 없음")
    void deleteTask_fail_accessDenied() {
        TaskDetailVO existing = makeTaskDetailVO(999L);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(CURRENT_EMP_ID);
            given(taskMapper.selectTaskDetail(TASK_ID)).willReturn(existing);
            given(projectMapper.selectProjectLeaderId(PROJ_ID)).willReturn(999L);

            assertThatThrownBy(() -> taskService.deleteTask(PROJ_ID, TASK_ID))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
        }
    }

    private TaskCreateRequest makeCreateRequest(List<Long> empIdList) {
        try {
            TaskCreateRequest request = new TaskCreateRequest();

            Field taskNm = TaskCreateRequest.class.getDeclaredField("taskNm");
            taskNm.setAccessible(true);
            taskNm.set(request, "테스트 업무");

            Field taskTypeCd = TaskCreateRequest.class.getDeclaredField("taskTypeCd");
            taskTypeCd.setAccessible(true);
            taskTypeCd.set(request, "01");

            Field taskStatCd = TaskCreateRequest.class.getDeclaredField("taskStatCd");
            taskStatCd.setAccessible(true);
            taskStatCd.set(request, "01");

            Field taskMngrId = TaskCreateRequest.class.getDeclaredField("taskMngrId");
            taskMngrId.setAccessible(true);
            taskMngrId.set(request, 1L);

            Field taskPriorityCd = TaskCreateRequest.class.getDeclaredField("taskPriorityCd");
            taskPriorityCd.setAccessible(true);
            taskPriorityCd.set(request, "01");

            Field taskImprtncCd = TaskCreateRequest.class.getDeclaredField("taskImprtncCd");
            taskImprtncCd.setAccessible(true);
            taskImprtncCd.set(request, "01");

            Field empIdListField = TaskCreateRequest.class.getDeclaredField("empIdList");
            empIdListField.setAccessible(true);
            empIdListField.set(request, empIdList);

            return request;
        } catch (Exception e) {
            throw new RuntimeException("TaskCreateRequest 생성 실패", e);
        }
    }

    private TaskUpdateRequest makeUpdateRequest(String taskStatCd) {
        try {
            TaskUpdateRequest request = new TaskUpdateRequest();

            Field statCd = TaskUpdateRequest.class.getDeclaredField("taskStatCd");
            statCd.setAccessible(true);
            statCd.set(request, taskStatCd);

            return request;
        } catch (Exception e) {
            throw new RuntimeException("TaskUpdateRequest 생성 실패", e);
        }
    }
}
