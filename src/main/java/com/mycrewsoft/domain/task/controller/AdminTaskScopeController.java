package com.mycrewsoft.domain.task.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;
import com.mycrewsoft.domain.task.service.AdminTaskScopeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/tasks")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자가 업무 범위 옵션을 관리하는 API")
public class AdminTaskScopeController {

    private final AdminTaskScopeService adminTaskScopeService;

    @Operation(summary = "업무의 범위를 받는 API", description = "역할 권한 설정 시 업무 범위 옵션을 반환하는 API")    @GetMapping("/scope-options")
    public ApiResponse<List<AdminScopeOptionResponseDTO>> getTaskScopeOptions() {
        return ApiResponse.success(adminTaskScopeService.getTaskScopeOptions());
    }
}
