package com.mycrewsoft.domain.employee.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
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
@Tag(name = "Admin Authorization", description = "Admin permission and role APIs")
public class AdminAuthorizationController {

    private final AdminAuthorizationService adminAuthorizationService;

    @Operation(summary = "Get permissions", description = "Returns all enabled permissions with details.")
    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponseDTO>> getPermissions() {
        return ApiResponse.success(adminAuthorizationService.getPermissions());
    }

    @Operation(summary = "Get roles", description = "Returns role list with permission and employee counts.")
    @GetMapping("/roles")
    public ApiResponse<List<RoleListResponseDTO>> getRoles() {
        return ApiResponse.success(adminAuthorizationService.getRoles());
    }

    @Operation(summary = "Get role detail", description = "Returns role detail with permissions and assigned employees.")
    @GetMapping("/roles/{roleId}")
    public ApiResponse<RoleDetailResponseDTO> getRole(@PathVariable Long roleId) {
        return ApiResponse.success(adminAuthorizationService.getRole(roleId));
    }

    @Operation(summary = "Create role", description = "Creates a role and maps multiple permissions.")
    @PostMapping("/roles")
    public ApiResponse<RoleDetailResponseDTO> createRole(
            @Valid @RequestBody RoleCreateRequestDTO request) {
        return ApiResponse.success(adminAuthorizationService.createRole(request));
    }

    @Operation(summary = "Update role", description = "Updates role name, description, and permissions.")
    @PutMapping("/roles/{roleId}")
    public ApiResponse<RoleDetailResponseDTO> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleUpdateRequestDTO request) {
        return ApiResponse.success(adminAuthorizationService.updateRole(roleId, request));
    }

    @Operation(summary = "Delete role", description = "Deletes a role and moves assigned employees to a replacement role.")
    @DeleteMapping("/roles/{roleId}")
    public ApiResponse<String> deleteRole(
            @PathVariable Long roleId,
            @RequestBody(required = false) RoleDeleteRequestDTO request) {
        adminAuthorizationService.deleteRole(roleId, request);
        return ApiResponse.success("Role deleted.");
    }

    @Operation(summary = "Assign role", description = "Assigns a role to multiple employees with a scope.")
    @PostMapping("/roles/{roleId}/assignments")
    public ApiResponse<RoleDetailResponseDTO> assignRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleAssignRequestDTO request) {
        return ApiResponse.success(adminAuthorizationService.assignRole(roleId, request));
    }

    @Operation(summary = "Revoke role", description = "Revokes a role from multiple employees.")
    @DeleteMapping("/roles/{roleId}/assignments")
    public ApiResponse<RoleDetailResponseDTO> revokeRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleRevokeRequestDTO request) {
        return ApiResponse.success(adminAuthorizationService.revokeRole(roleId, request));
    }
}
