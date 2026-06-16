package com.mycrewsoft.domain.task.service;

import java.util.List;

import com.mycrewsoft.domain.task.dto.request.TaskCreateRequest;
import com.mycrewsoft.domain.task.dto.request.TaskUpdateRequest;
import com.mycrewsoft.domain.task.dto.response.TaskDetailResponse;
import com.mycrewsoft.domain.task.dto.response.TaskListResponse;

public interface TaskService {

	/**
     * 프로젝트 ID로 업무 목록을 조회하는 메서드
     * @param projId 프로젝트 ID
     * @return List<TaskListResponse>
     */
    List<TaskListResponse> getTaskList(Long projId);

    /**
     * 업무 ID로 업무 상세를 조회하는 메서드
     * @param taskId 업무 ID
     * @return TaskDetailResponse
     */
    TaskDetailResponse getTaskDetail(Long projId, Long taskId);

    /**
     * 업무를 생성하는 메서드
     * @param request 업무 생성 요청 DTO
     * @return 생성된 taskId
     */
    Long createTask(Long projId, TaskCreateRequest request);

    /**
     * 업무 정보를 수정하는 메서드
     * @param taskId  수정할 업무 ID
     * @param request 업무 수정 요청 DTO
     */
    void updateTask(Long projId, Long taskId, TaskUpdateRequest request);

    /**
     * 업무 담당자를 변경하는 메서드
     * @param taskId     업무 ID
     * @param taskMngrId 새 담당자 사번
     */
    void updateTaskManager(Long projId, Long taskId, Long taskMngrId);

    /**
     * 업무 참여자를 추가하는 메서드
     * @param taskId 업무 ID
     * @param empId  추가할 참여자 사번
     */
    void addTaskPtcpts(Long projId, Long taskId, List<Long> empIdList);

    /**
     * 업무 참여자를 삭제하는 메서드
     * @param taskId 업무 ID
     * @param empId  삭제할 참여자 사번
     */
    void removeTaskPtcpts(Long projId, Long taskId, List<Long> empIdList);

    /**
     * 업무를 논리 삭제하는 메서드
     * @param taskId 삭제할 업무 ID
     */
    void deleteTask(Long projId, Long taskId);
    
    // 위젯용
    List<TaskListResponse> getTaskListForWidget();
}
