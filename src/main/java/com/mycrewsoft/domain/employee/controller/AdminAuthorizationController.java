package com.mycrewsoft.domain.employee.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.request.PermissionStatusUpdateRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleAssignRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleCreateRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleDeleteRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleRevokeRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.RoleUpdateRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.PermissionResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleDetailResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.RoleListResponseDTO;
import com.mycrewsoft.domain.employee.service.AdminAuthorizationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자가 직원 권한과 역할을 관리하는 API")
public class AdminAuthorizationController {

    private final AdminAuthorizationService adminAuthorizationService;

    @Operation(summary = "권한 목록 불러오기", description = "시스템에서 제공하는 모든 권한 목록을 반환.")
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponseDTO>>> getPermissions() {
        return ResponseEntity.ok(ApiResponse.success(adminAuthorizationService.getPermissions()));
    }

    @Operation(summary = "권한 사용여부 결정", description = "시스템에서 해당 권한의 사용여부를 결정.")
    @PutMapping("/permissions/{permissionId}/status")
    public ResponseEntity<ApiResponse<PermissionResponseDTO>> updatePermissionStatus(
            @PathVariable Long permissionId,
            @Valid @RequestBody PermissionStatusUpdateRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(adminAuthorizationService.updatePermissionStatus(permissionId, request)));
    }

    @Operation(summary = "역할 목록 불러오기", description = "시스템에 존재하는 모든 역할을 반환.")
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleListResponseDTO>>> getRoles() {
        return ResponseEntity.ok(ApiResponse.success(adminAuthorizationService.getRoles()));
    }

    @Operation(summary = "역할 상세 보기", description = "해당 역할의 상세 정보를 반환.")
    @GetMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse<RoleDetailResponseDTO>> getRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(ApiResponse.success(adminAuthorizationService.getRole(roleId)));
    }

    @Operation(summary = "역할 생성", description = "한 역할을 생성하는 API. 역할 이름, 설명, 권한을 입력받아 새로운 역할을 생성.")
    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<RoleDetailResponseDTO>> createRole(
            @Valid @RequestBody RoleCreateRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(adminAuthorizationService.createRole(request)));
    }

    @Operation(summary = "역할 수정", description = "한 역할을 수정하는 API. 역할 이름, 설명, 권한을 입력받아 기존 역할을 수정.")
    @PutMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse<RoleDetailResponseDTO>> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleUpdateRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(adminAuthorizationService.updateRole(roleId, request)));
    }

    @Operation(summary = "역할 삭제", description = "한 역할을 삭제하는 API. 역할 ID를 입력받아 해당 역할을 삭제.")
    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse<String>> deleteRole(
            @PathVariable Long roleId,
            @RequestBody(required = false) RoleDeleteRequestDTO request) {
        adminAuthorizationService.deleteRole(roleId, request);
        return ResponseEntity.ok(ApiResponse.success("Role deleted."));
    }

    @Operation(summary = "역할 배정", description = "역할을 다수의 직원에게 배정하는 API.")
    @PostMapping("/roles/{roleId}/assignments")
    public ResponseEntity<ApiResponse<RoleDetailResponseDTO>> assignRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleAssignRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(adminAuthorizationService.assignRole(roleId, request)));
    }

    @Operation(summary = "역할 회수", description = "역할을 직원으로부터 회수하는 API.")
    @DeleteMapping("/roles/{roleId}/assignments")
    public ResponseEntity<ApiResponse<RoleDetailResponseDTO>> revokeRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleRevokeRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(adminAuthorizationService.revokeRole(roleId, request)));
    }
}
