package com.mycrewsoft.domain.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.project.mapper.ProjectMapper;
import com.mycrewsoft.domain.projectmember.mapper.ProjectMemberMapper;
import com.mycrewsoft.domain.projectmember.vo.ProjectMemberVO;
import com.mycrewsoft.domain.task.dto.request.TaskCreateRequest;
import com.mycrewsoft.domain.task.dto.request.TaskUpdateRequest;
import com.mycrewsoft.domain.task.dto.response.TaskDetailResponse;
import com.mycrewsoft.domain.task.dto.response.TaskListResponse;
import com.mycrewsoft.domain.task.mapper.TaskDtoMapper;
import com.mycrewsoft.domain.task.mapper.TaskMapper;
import com.mycrewsoft.domain.task.vo.TaskDetailVO;
import com.mycrewsoft.domain.task.vo.TaskListVO;
import com.mycrewsoft.domain.task.vo.TaskPtcptVO;
import com.mycrewsoft.domain.task.vo.TaskVO;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("업무 서비스 단위 테스트")
class TaskServiceImplTest {

    @InjectMocks
    private TaskServiceImpl taskService;

    @Mock private TaskMapper taskMapper;
    @Mock private TaskDtoMapper taskDtoMapper;
    @Mock private ProjectMemberMapper projectMemberMapper;
    @Mock private ProjectMapper projectMapper;

    // ── 공통 픽스처 헬퍼 ──────────────────────────────────────────

    private ProjectMemberVO memberVO(Long empId) {
        ProjectMemberVO vo = new ProjectMemberVO();
        vo.setEmpId(empId);
        return vo;
    }

    private TaskDetailVO detailVO(Long taskId, Long mngrId, Long frstRgtrId) {
        TaskDetailVO vo = new TaskDetailVO();
        vo.setTaskId(taskId);
        vo.setTaskMngrId(mngrId);
        vo.setFrstRgtrId(frstRgtrId);
        return vo;
    }

    // ─────────────────────────────────────────────────────────────
    //  GET TASK LIST
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("업무 목록 조회 - 성공")
    void getTaskList_success() {
        Long projId = 1L;
        Long empId = 100L;
        List<TaskListVO> voList = List.of(new TaskListVO());
        List<TaskListResponse> responseList = List.of(new TaskListResponse());

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(projectMemberMapper.selectProjectMemberList(projId))
                    .willReturn(List.of(memberVO(empId)));
            given(taskMapper.selectTaskList(projId)).willReturn(voList);
            given(taskDtoMapper.toListResponseList(voList)).willReturn(responseList);

            List<TaskListResponse> result = taskService.getTaskList(projId);

            assertThat(result).hasSize(1);
            verify(taskMapper).selectTaskList(projId);
        }
    }

    @Test
    @DisplayName("업무 목록 조회 - 프로젝트 참여자 아닐 때 예외")
    void getTaskList_notParticipant_fail() {
        Long projId = 1L;
        Long empId = 100L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(projectMemberMapper.selectProjectMemberList(projId))
                    .willReturn(List.of(memberVO(999L)));

            assertThatThrownBy(() -> taskService.getTaskList(projId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_PARTICIPANT);

            verify(taskMapper, never()).selectTaskList(any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  GET TASK DETAIL
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("업무 상세 조회 - 성공")
    void getTaskDetail_success() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;
        TaskDetailVO vo = detailVO(taskId, empId, empId);
        TaskDetailResponse response = new TaskDetailResponse();

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(projectMemberMapper.selectProjectMemberList(projId))
                    .willReturn(List.of(memberVO(empId)));
            given(taskMapper.selectTaskDetail(taskId)).willReturn(vo);
            given(taskDtoMapper.toDetailResponse(vo)).willReturn(response);

            TaskDetailResponse result = taskService.getTaskDetail(projId, taskId);

            assertThat(result).isNotNull();
        }
    }

    @Test
    @DisplayName("업무 상세 조회 - 존재하지 않는 업무 예외")
    void getTaskDetail_notFound_fail() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(projectMemberMapper.selectProjectMemberList(projId))
                    .willReturn(List.of(memberVO(empId)));
            given(taskMapper.selectTaskDetail(taskId)).willReturn(null);

            assertThatThrownBy(() -> taskService.getTaskDetail(projId, taskId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_FOUND);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  CREATE TASK
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("업무 등록 - 성공 (참여자 없음)")
    void createTask_success_noPtcpt() {
        Long projId = 1L;
        Long empId = 100L;
        TaskCreateRequest request = new TaskCreateRequest();
        TaskVO vo = new TaskVO();
        vo.setTaskId(10L);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(projectMemberMapper.selectProjectMemberList(projId))
                    .willReturn(List.of(memberVO(empId)));
            given(taskDtoMapper.toVO(request)).willReturn(vo);
            given(taskMapper.insertTask(vo)).willReturn(1);

            Long taskId = taskService.createTask(projId, request);

            assertThat(taskId).isEqualTo(10L);
            verify(taskMapper).insertTask(vo);
            verify(taskMapper, never()).insertTaskPtcpt(any());
        }
    }

    @Test
    @DisplayName("업무 등록 - 성공 (참여자 2명 포함)")
    void createTask_success_withPtcpts() {
        Long projId = 1L;
        Long empId = 100L;
        TaskCreateRequest request = new TaskCreateRequest();
        setField(request, "empIdList", List.of(200L, 300L));
        TaskVO vo = new TaskVO();
        vo.setTaskId(10L);
        TaskPtcptVO ptcptVO = new TaskPtcptVO();

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(projectMemberMapper.selectProjectMemberList(projId))
                    .willReturn(List.of(memberVO(empId)));
            given(taskDtoMapper.toVO(request)).willReturn(vo);
            given(taskDtoMapper.toPtcptVO(anyLong(), anyLong())).willReturn(ptcptVO);
            given(taskMapper.insertTask(vo)).willReturn(1);
            given(taskMapper.insertTaskPtcpt(ptcptVO)).willReturn(1);

            Long taskId = taskService.createTask(projId, request);

            assertThat(taskId).isEqualTo(10L);
            verify(taskMapper, times(2)).insertTaskPtcpt(ptcptVO);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE TASK
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("업무 수정 - 성공 (참여자 수정 없음)")
    void updateTask_success_noPtcptChange() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;
        TaskUpdateRequest request = new TaskUpdateRequest();
        TaskVO vo = new TaskVO();

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(projectMemberMapper.selectProjectMemberList(projId))
                    .willReturn(List.of(memberVO(empId)));
            given(taskMapper.selectTaskDetail(taskId)).willReturn(detailVO(taskId, empId, empId));
            given(taskDtoMapper.toVO(request)).willReturn(vo);
            willDoNothing().given(taskMapper).updateTask(taskId, vo);

            assertThatCode(() -> taskService.updateTask(projId, taskId, request))
                    .doesNotThrowAnyException();

            verify(taskMapper, never()).deleteTaskPtcpts(any());
        }
    }

    @Test
    @DisplayName("업무 수정 - 성공 (참여자 DELETE → INSERT)")
    void updateTask_success_withPtcptChange() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;
        TaskUpdateRequest request = new TaskUpdateRequest();
        setField(request, "empIdList", List.of(200L, 300L));
        TaskVO vo = new TaskVO();
        TaskPtcptVO ptcptVO = new TaskPtcptVO();

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(projectMemberMapper.selectProjectMemberList(projId))
                    .willReturn(List.of(memberVO(empId)));
            given(taskMapper.selectTaskDetail(taskId)).willReturn(detailVO(taskId, empId, empId));
            given(taskDtoMapper.toVO(request)).willReturn(vo);
            given(taskDtoMapper.toPtcptVO(anyLong(), anyLong())).willReturn(ptcptVO);
            willDoNothing().given(taskMapper).updateTask(taskId, vo);
            given(taskMapper.deleteTaskPtcpts(taskId)).willReturn(1);
            given(taskMapper.insertTaskPtcpt(ptcptVO)).willReturn(1);

            assertThatCode(() -> taskService.updateTask(projId, taskId, request))
                    .doesNotThrowAnyException();

            verify(taskMapper).deleteTaskPtcpts(taskId);
            verify(taskMapper, times(2)).insertTaskPtcpt(ptcptVO);
        }
    }

    @Test
    @DisplayName("업무 수정 - 존재하지 않는 업무 예외")
    void updateTask_notFound_fail() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(projectMemberMapper.selectProjectMemberList(projId))
                    .willReturn(List.of(memberVO(empId)));
            given(taskMapper.selectTaskDetail(taskId)).willReturn(null);

            assertThatThrownBy(() -> taskService.updateTask(projId, taskId, new TaskUpdateRequest()))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_FOUND);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE TASK MANAGER
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("업무 담당자 변경 - 담당자 본인이 변경 성공")
    void updateTaskManager_byManager_success() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;
        Long newMngrId = 200L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(taskMapper.selectTaskDetail(taskId)).willReturn(detailVO(taskId, empId, empId));
            given(projectMapper.selectProjectLeaderId(projId)).willReturn(999L);
            given(taskMapper.updateTaskManager(taskId, newMngrId, empId)).willReturn(1);

            assertThatCode(() -> taskService.updateTaskManager(projId, taskId, newMngrId))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("업무 담당자 변경 - 프로젝트장이 변경 성공")
    void updateTaskManager_byProjectLeader_success() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;
        Long newMngrId = 200L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(taskMapper.selectTaskDetail(taskId)).willReturn(detailVO(taskId, 999L, 999L));
            given(projectMapper.selectProjectLeaderId(projId)).willReturn(empId);
            given(taskMapper.updateTaskManager(taskId, newMngrId, empId)).willReturn(1);

            assertThatCode(() -> taskService.updateTaskManager(projId, taskId, newMngrId))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("업무 담당자 변경 - 담당자도 프로젝트장도 아닐 때 예외")
    void updateTaskManager_notAuthorized_fail() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(taskMapper.selectTaskDetail(taskId)).willReturn(detailVO(taskId, 999L, 999L));
            given(projectMapper.selectProjectLeaderId(projId)).willReturn(888L);

            assertThatThrownBy(() -> taskService.updateTaskManager(projId, taskId, 200L))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  ADD TASK PTCPTS
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("업무 참여자 추가 - 성공")
    void addTaskPtcpts_success() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;
        List<Long> empIdList = List.of(200L, 300L);
        TaskPtcptVO ptcptVO = new TaskPtcptVO();

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(taskMapper.selectTaskDetail(taskId)).willReturn(detailVO(taskId, empId, empId));
            given(projectMapper.selectProjectLeaderId(projId)).willReturn(999L);
            given(taskDtoMapper.toPtcptVO(anyLong(), anyLong())).willReturn(ptcptVO);
            given(taskMapper.insertTaskPtcpt(ptcptVO)).willReturn(1);

            assertThatCode(() -> taskService.addTaskPtcpts(projId, taskId, empIdList))
                    .doesNotThrowAnyException();

            verify(taskMapper, times(2)).insertTaskPtcpt(ptcptVO);
        }
    }

    @Test
    @DisplayName("업무 참여자 추가 - 존재하지 않는 업무 예외")
    void addTaskPtcpts_notFound_fail() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            given(taskMapper.selectTaskDetail(taskId)).willReturn(null);

            assertThatThrownBy(() -> taskService.addTaskPtcpts(projId, taskId, List.of(200L)))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_FOUND);

            verify(taskMapper, never()).insertTaskPtcpt(any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  REMOVE TASK PTCPTS
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("업무 참여자 삭제 - 성공")
    void removeTaskPtcpts_success() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;
        List<Long> empIdList = List.of(200L, 300L);

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(taskMapper.selectTaskDetail(taskId)).willReturn(detailVO(taskId, empId, empId));
            given(projectMapper.selectProjectLeaderId(projId)).willReturn(999L);
            given(taskMapper.deleteTaskPtcpt(eq(taskId), anyLong())).willReturn(1);

            assertThatCode(() -> taskService.removeTaskPtcpts(projId, taskId, empIdList))
                    .doesNotThrowAnyException();

            verify(taskMapper, times(2)).deleteTaskPtcpt(eq(taskId), anyLong());
        }
    }

    @Test
    @DisplayName("업무 참여자 삭제 - 존재하지 않는 참여자 예외")
    void removeTaskPtcpts_ptcptNotFound_fail() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(taskMapper.selectTaskDetail(taskId)).willReturn(detailVO(taskId, empId, empId));
            given(projectMapper.selectProjectLeaderId(projId)).willReturn(999L);
            given(taskMapper.deleteTaskPtcpt(eq(taskId), anyLong())).willReturn(0);

            assertThatThrownBy(() -> taskService.removeTaskPtcpts(projId, taskId, List.of(200L)))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_PARTICIPANT);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  DELETE TASK
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("업무 삭제 - 성공")
    void deleteTask_success() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(taskMapper.selectTaskDetail(taskId)).willReturn(detailVO(taskId, empId, empId));
            given(taskMapper.deleteTaskPtcpts(taskId)).willReturn(1);
            given(taskMapper.deleteTask(taskId, empId)).willReturn(1);

            assertThatCode(() -> taskService.deleteTask(projId, taskId))
                    .doesNotThrowAnyException();

            InOrder inOrder = inOrder(taskMapper);
            inOrder.verify(taskMapper).deleteTaskPtcpts(taskId);
            inOrder.verify(taskMapper).deleteTask(taskId, empId);
        }
    }

    @Test
    @DisplayName("업무 삭제 - 담당자도 프로젝트장도 아닐 때 예외")
    void deleteTask_notOwner_fail() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);

            given(taskMapper.selectTaskDetail(taskId)).willReturn(detailVO(taskId, 999L, 999L));
            given(projectMapper.selectProjectLeaderId(projId)).willReturn(888L);

            assertThatThrownBy(() -> taskService.deleteTask(projId, taskId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);

            verify(taskMapper, never()).deleteTaskPtcpts(any());
            verify(taskMapper, never()).deleteTask(any(), any());
        }
    }

    @Test
    @DisplayName("업무 삭제 - 존재하지 않는 업무 예외")
    void deleteTask_notFound_fail() {
        Long projId = 1L;
        Long taskId = 10L;
        Long empId = 100L;

        try (MockedStatic<SecurityUtil> secUtil = Mockito.mockStatic(SecurityUtil.class)) {
            secUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(empId);
            given(taskMapper.selectTaskDetail(taskId)).willReturn(null);

            assertThatThrownBy(() -> taskService.deleteTask(projId, taskId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_FOUND);
        }
    }
    
    private <T> void setField(T target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}