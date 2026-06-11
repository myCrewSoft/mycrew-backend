package com.mycrewsoft.domain.task.service;

import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.project.mapper.ProjectMapper;
import com.mycrewsoft.domain.projectmember.mapper.ProjectMemberMapper;
import com.mycrewsoft.domain.projectmember.vo.ProjectMemberVO;
import com.mycrewsoft.domain.task.dto.request.TaskCreateRequest;
import com.mycrewsoft.domain.task.dto.request.TaskUpdateRequest;
import com.mycrewsoft.domain.task.dto.response.TaskDetailResponse;
import com.mycrewsoft.domain.task.dto.response.TaskListResponse;
import com.mycrewsoft.domain.task.event.TaskAssignedEvent;
import com.mycrewsoft.domain.task.event.TaskCancelledEvent;
import com.mycrewsoft.domain.task.event.TaskDeadlineChangedEvent;
import com.mycrewsoft.domain.task.event.TaskManagerChangedEvent;
import com.mycrewsoft.domain.task.event.TaskMemberRemovedEvent;
import com.mycrewsoft.domain.task.event.TaskStatusChangedEvent;
import com.mycrewsoft.domain.task.mapper.TaskDtoMapper;
import com.mycrewsoft.domain.task.mapper.TaskMapper;
import com.mycrewsoft.domain.task.vo.TaskDetailVO;
import com.mycrewsoft.domain.task.vo.TaskListVO;
import com.mycrewsoft.domain.task.vo.TaskPtcptVO;
import com.mycrewsoft.domain.task.vo.TaskVO;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final TaskDtoMapper taskDtoMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectMapper projectMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    public List<TaskListResponse> getTaskList(Long projId) {
        // 본인 currentEmpId 조회
        Long currentEmpId = getCurrentEmpIdOrThrow();

        // 해당 프로젝트의 참여자인지 확인
        validateProjectParticipant(currentEmpId, projId);

        // DB에서 데이터 조회
        List<TaskListVO> taskList = taskMapper.selectTaskList(projId);

        // VO -> DTO 변환
        return taskDtoMapper.toListResponseList(taskList);
    }

    @Override
    public TaskDetailResponse getTaskDetail(Long projId, Long taskId) {
        // 본인 currentEmpId 조회
        Long currentEmpId = getCurrentEmpIdOrThrow();

        // 해당 프로젝트의 참여자인지 확인
        validateProjectParticipant(currentEmpId, projId);

        // DB에서 데이터 조회
        TaskDetailVO task = taskMapper.selectTaskDetail(taskId);
        if (task == null) throw new CustomException(ErrorCode.TASK_NOT_FOUND);

        // VO -> DTO 변환
        return taskDtoMapper.toDetailResponse(task);
    }

    @Override
    @Transactional
    public Long createTask(Long projId, TaskCreateRequest request) {
        // 본인 currentEmpId 조회
        Long currentEmpId = getCurrentEmpIdOrThrow();

        // 해당 프로젝트의 참여자인지 확인
        validateProjectParticipant(currentEmpId, projId);

        // DTO -> VO 변환
        TaskVO task = taskDtoMapper.toVO(request);
        task.setFrstRgtrId(currentEmpId);
        task.setLastMdfrId(currentEmpId);
        task.setTaskStatCd("00");

        // DB에 저장
        taskMapper.insertTask(task);

        // 참여자 등록
        if (request.getEmpIdList() != null && !request.getEmpIdList().isEmpty()) {
            for (Long empId : request.getEmpIdList()) {
                TaskPtcptVO ptcptVO = taskDtoMapper.toPtcptVO(task.getTaskId(), empId);
                taskMapper.insertTaskPtcpt(ptcptVO);
            }

            // 업무 배정 알림
            eventPublisher.publishEvent(
                new TaskAssignedEvent(task.getTaskNm(), request.getEmpIdList())
            );
        }

        return task.getTaskId();
    }

    @Override
    @Transactional
    public void updateTask(Long projId, Long taskId, TaskUpdateRequest request) {
        // 본인 currentEmpId 조회
        Long currentEmpId = getCurrentEmpIdOrThrow();

        // 해당 프로젝트의 참여자인지 확인
        validateProjectParticipant(currentEmpId, projId);

        // 존재하는 업무인지 확인
        TaskDetailVO existing = taskMapper.selectTaskDetail(taskId);
        if (existing == null) throw new CustomException(ErrorCode.TASK_NOT_FOUND);

        // DTO -> VO 변환
        TaskVO task = taskDtoMapper.toVO(request);
        task.setLastMdfrId(currentEmpId);

        // DB에 저장
        taskMapper.updateTask(taskId, task);

        // 참여자 수정 요청이 있을 때만 DELETE → INSERT
        if (request.getEmpIdList() != null) {
            taskMapper.deleteTaskPtcpts(taskId);
            for (Long empId : request.getEmpIdList()) {
                TaskPtcptVO ptcptVO = taskDtoMapper.toPtcptVO(taskId, empId);
                taskMapper.insertTaskPtcpt(ptcptVO);
            }
        }

        // 마감일 변경 알림
        if (request.getTaskEndDt() != null &&
            !request.getTaskEndDt().equals(existing.getTaskEndDt())) {
            eventPublisher.publishEvent(
                new TaskDeadlineChangedEvent(existing.getTaskNm(), existing.getRcvrEmpIds())
            );
        }

        // 상태 변경 알림
        if (request.getTaskStatCd() != null &&
            !request.getTaskStatCd().equals(existing.getTaskStatCd())) {
            eventPublisher.publishEvent(
                new TaskStatusChangedEvent(existing.getTaskNm(), existing.getRcvrEmpIds())
            );
        }
    }

    @Override
    @Transactional
    public void updateTaskManager(Long projId, Long taskId, Long taskMngrId) {
        // 본인 currentEmpId 조회
        Long currentEmpId = getCurrentEmpIdOrThrow();

        // 존재하는 업무인지 확인
        TaskDetailVO existing = taskMapper.selectTaskDetail(taskId);
        if (existing == null) throw new CustomException(ErrorCode.TASK_NOT_FOUND);

        // 해당 업무의 담당자거나 프로젝트장인지 확인
        validateTaskManagerOrProjectLeader(currentEmpId, projId, existing);

        // DB에 저장
        taskMapper.updateTaskManager(taskId, taskMngrId, currentEmpId);

        // 업무 담당자 변경 알림
        eventPublisher.publishEvent(
            new TaskManagerChangedEvent(existing.getTaskNm(), existing.getTaskMngrNm(), existing.getRcvrEmpIds())
        );
    }

    @Override
    @Transactional
    public void addTaskPtcpts(Long projId, Long taskId, List<Long> empIdList) {
        // 본인 currentEmpId 조회
        Long currentEmpId = getCurrentEmpIdOrThrow();

        // 존재하는 업무인지 확인
        TaskDetailVO existing = taskMapper.selectTaskDetail(taskId);
        if (existing == null) throw new CustomException(ErrorCode.TASK_NOT_FOUND);

        // 해당 업무의 담당자거나 프로젝트장인지 확인
        validateTaskManagerOrProjectLeader(currentEmpId, projId, existing);

        // DB에 생성
        for (Long empId : empIdList) {
            TaskPtcptVO ptcptVO = taskDtoMapper.toPtcptVO(taskId, empId);
            taskMapper.insertTaskPtcpt(ptcptVO);
        }

        // 업무 배정 알림
        eventPublisher.publishEvent(
            new TaskAssignedEvent(existing.getTaskNm(), empIdList)
        );
    }

    @Override
    @Transactional
    public void removeTaskPtcpts(Long projId, Long taskId, List<Long> empIdList) {
        // 본인 currentEmpId 조회
        Long currentEmpId = getCurrentEmpIdOrThrow();

        // 존재하는 업무인지 확인
        TaskDetailVO existing = taskMapper.selectTaskDetail(taskId);
        if (existing == null) throw new CustomException(ErrorCode.TASK_NOT_FOUND);

        // 해당 업무의 담당자거나 프로젝트장인지 확인
        validateTaskManagerOrProjectLeader(currentEmpId, projId, existing);

        // DB에서 삭제
        for (Long empId : empIdList) {
            int result = taskMapper.deleteTaskPtcpt(taskId, empId);
            if (result == 0) throw new CustomException(ErrorCode.TASK_NOT_PARTICIPANT);
        }
        // 업무 제외 알림
        eventPublisher.publishEvent(
            new TaskMemberRemovedEvent(existing.getTaskNm(), empIdList)
        );
    }

    @Override
    @Transactional
    public void deleteTask(Long projId, Long taskId) {
        // 본인 currentEmpId 조회
        Long currentEmpId = getCurrentEmpIdOrThrow();

        // 존재하는 업무인지 확인
        TaskDetailVO existing = taskMapper.selectTaskDetail(taskId);
        if (existing == null) throw new CustomException(ErrorCode.TASK_NOT_FOUND);

        // 해당 업무의 담당자거나 프로젝트장인지 확인
        validateTaskManagerOrProjectLeader(currentEmpId, projId, existing);

        // 업무 취소 알림
        eventPublisher.publishEvent(
            new TaskCancelledEvent(existing.getTaskNm(), existing.getRcvrEmpIds())
        );

        // 참여자 먼저 삭제 후 업무 논리 삭제
        taskMapper.deleteTaskPtcpts(taskId);
        taskMapper.deleteTask(taskId, currentEmpId);
    }

    // ── private 헬퍼 ──────────────────────────────────────────────

    private Long getCurrentEmpIdOrThrow() {
        Long empId = SecurityUtil.getCurrentEmpId();
        if (empId == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return empId;
    }

    private void validateProjectParticipant(Long currentEmpId, Long projId) {
        List<ProjectMemberVO> projectMemberList =
                projectMemberMapper.selectProjectMemberList(projId);
        boolean isProjectMember = projectMemberList.stream()
                .anyMatch(member -> Objects.equals(member.getEmpId(), currentEmpId));
        if (!isProjectMember) throw new CustomException(ErrorCode.PROJECT_NOT_PARTICIPANT);
    }

    // 담당자만 가능 → TASK_NOT_OWNER
    private void validateTaskManager(Long currentEmpId, TaskDetailVO task) {
        if (!Objects.equals(task.getTaskMngrId(), currentEmpId)) {
            throw new CustomException(ErrorCode.TASK_NOT_OWNER);
        }
    }

    // 담당자 또는 프로젝트장 가능
    private void validateTaskManagerOrProjectLeader(Long currentEmpId, Long projId, TaskDetailVO task) {
        boolean isTaskManager = Objects.equals(task.getTaskMngrId(), currentEmpId);
        Long projectLeaderId = projectMapper.selectProjectLeaderId(projId);
        boolean isProjectLeader = Objects.equals(projectLeaderId, currentEmpId);

        if (!isTaskManager && !isProjectLeader) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}