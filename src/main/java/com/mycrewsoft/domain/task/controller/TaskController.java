package com.mycrewsoft.domain.task.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.task.dto.request.TaskCreateRequest;
import com.mycrewsoft.domain.task.dto.request.TaskUpdateRequest;
import com.mycrewsoft.domain.task.dto.response.TaskDetailResponse;
import com.mycrewsoft.domain.task.dto.response.TaskListResponse;
import com.mycrewsoft.domain.task.service.TaskService;
import com.mycrewsoft.validate.groups.InsertGroup;
import com.mycrewsoft.validate.groups.UpdateGroup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "업무", description = "프로젝트 업무 관리 API")
@RestController
@RequestMapping("/projects/{projId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "업무 목록 조회", description = "프로젝트 ID로 해당 프로젝트의 업무 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskListResponse>>> getTaskList(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projId) {

        List<TaskListResponse> result = taskService.getTaskList(projId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "업무 상세 조회", description = "업무 ID로 업무 상세 정보와 참여자 목록을 조회합니다.")
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskDetailResponse>> getTaskDetail(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projId,
            @Parameter(description = "업무 ID") @PathVariable Long taskId) {

        TaskDetailResponse result = taskService.getTaskDetail(projId, taskId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "업무 등록", description = "프로젝트에 새로운 업무를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createTask(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projId,
            @Validated(InsertGroup.class) @RequestBody TaskCreateRequest request) {

        Long taskId = taskService.createTask(projId, request);
        return ResponseEntity.ok(ApiResponse.success("업무가 등록되었습니다.", taskId));
    }

    @Operation(summary = "업무 수정", description = "업무 정보를 수정합니다. 변경할 필드만 전송하면 됩니다.")
    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> updateTask(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projId,
            @Parameter(description = "업무 ID") @PathVariable Long taskId,
            @Validated(UpdateGroup.class) @RequestBody TaskUpdateRequest request) {

        taskService.updateTask(projId, taskId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "업무 담당자 변경", description = "업무 담당자를 변경합니다. 현재 담당자 또는 프로젝트장만 가능합니다.")
    @PatchMapping("/{taskId}/manager")
    public ResponseEntity<ApiResponse<Void>> updateTaskManager(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projId,
            @Parameter(description = "업무 ID") @PathVariable Long taskId,
            @Parameter(description = "새 담당자 사번") @RequestBody Long taskMngrId) {

        taskService.updateTaskManager(projId, taskId, taskMngrId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "업무 참여자 추가", description = "업무에 참여자를 추가합니다. 1명 이상 전송 가능합니다.")
    @PostMapping("/{taskId}/ptcpts")
    public ResponseEntity<ApiResponse<String>> addTaskPtcpts(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projId,
            @Parameter(description = "업무 ID") @PathVariable Long taskId,
            @RequestBody List<Long> empIdList) {

        taskService.addTaskPtcpts(projId, taskId, empIdList);
        return ResponseEntity.ok(ApiResponse.success("참여자가 추가되었습니다."));
    }

    @Operation(summary = "업무 참여자 삭제", description = "업무 참여자를 삭제합니다. 1명 이상 전송 가능합니다.")
    @DeleteMapping("/{taskId}/ptcpts")
    public ResponseEntity<ApiResponse<String>> removeTaskPtcpts(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projId,
            @Parameter(description = "업무 ID") @PathVariable Long taskId,
            @RequestBody List<Long> empIdList) {

        taskService.removeTaskPtcpts(projId, taskId, empIdList);
        return ResponseEntity.ok(ApiResponse.success("참여자가 삭제되었습니다."));
    }

    @Operation(summary = "업무 삭제", description = "업무를 논리 삭제합니다. 업무 생성자만 가능합니다.")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<String>> deleteTask(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projId,
            @Parameter(description = "업무 ID") @PathVariable Long taskId) {

        taskService.deleteTask(projId, taskId);
        return ResponseEntity.ok(ApiResponse.success("업무가 삭제되었습니다."));
    }
}