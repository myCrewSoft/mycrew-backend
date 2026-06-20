package com.mycrewsoft.domain.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.response.AdminScopeOptionResponseDTO;
import com.mycrewsoft.domain.project.dto.AdminProjectListResponseDto;
import com.mycrewsoft.domain.project.service.AdminProjectScopeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/projects")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자가 프로젝트 범위 옵션을 관리하는 API")
public class AdminProjectScopeController {

    private final AdminProjectScopeService adminProjectScopeService;

    @Operation(summary = "프로젝트의 범위를 받는 API", description = "역할 권한 설정 시 프로젝트 범위 옵션을 반환하는 API")
    @GetMapping("/scope-options")
    public ResponseEntity<ApiResponse<List<AdminScopeOptionResponseDTO>>> getProjectScopeOptions() {
        return ResponseEntity.ok(ApiResponse.success(adminProjectScopeService.getProjectScopeOptions()));
    }
    
    @Operation(summary = "프로젝트 관리자 페이지 전체 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminProjectListResponseDto>>> getAdminProjectList(){
    	
    	return ResponseEntity.ok(ApiResponse.success(adminProjectScopeService.getAdminProjectList()));
    }
}
