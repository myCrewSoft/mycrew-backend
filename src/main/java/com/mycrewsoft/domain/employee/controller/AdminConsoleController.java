package com.mycrewsoft.domain.employee.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.response.AdminMeResponseDTO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminConsoleController {

    private final AuthorizationService authorizationService;

    @Operation(summary = "관리자 페이지 접속 여부", description = "관리자 페이지에 접속할 수 있는 지 판단하는 API입니다.")
    @GetMapping("/access")
    public ResponseEntity<ApiResponse<AdminMeResponseDTO>> getAdminMe() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.ADMIN)
                .build();

        authorizationService.assertCurrentUserPermission(
                PermissionCode.ADMIN_CONSOLE_ACCESS,
                resource
        );

        Long empId = SecurityUtil.getCurrentEmpId();

        AdminMeResponseDTO response = AdminMeResponseDTO.builder()
                .empId(empId)
                .adminAccessible(true)
                .build();
        log.info("Admin console accessed by empId={}", empId);
        log.info("AdminMeResponseDTO: {}", response.isAdminAccessible());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
