package com.mycrewsoft.domain.department.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.department.service.AdminDepartmentScopeService;
import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/departments")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자가 부서 범위 옵션을 관리하는 API")
public class AdminDepartmentScopeController {

    private final AdminDepartmentScopeService adminDepartmentScopeService;

    @Operation(summary = "부서의 범위를 받는 API", description = "역할 권한 설정 시 부서 범위 옵션을 반환하는 API")    @GetMapping("/scope-options")
    public ResponseEntity<ApiResponse<List<AdminScopeOptionResponseDTO>>> getDepartmentScopeOptions() {
        return ResponseEntity.ok(ApiResponse.success(adminDepartmentScopeService.getDepartmentScopeOptions()));
    }
}
