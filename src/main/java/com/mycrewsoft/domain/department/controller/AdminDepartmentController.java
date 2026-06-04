package com.mycrewsoft.domain.department.controller;

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
import com.mycrewsoft.domain.department.dto.request.DepartmentCreateRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentDeleteRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentMemberAssignRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentMemberTransferRequestDTO;
import com.mycrewsoft.domain.department.dto.request.DepartmentUpdateRequestDTO;
import com.mycrewsoft.domain.department.dto.response.AdminDepartmentMemberResponseDTO;
import com.mycrewsoft.domain.department.dto.response.AdminDepartmentResponseDTO;
import com.mycrewsoft.domain.department.dto.response.DepartmentMemberMutationResponseDTO;
import com.mycrewsoft.domain.department.service.AdminDepartmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/departments")
@Tag(name = "관리자 - 부서", description = "관리자 부서 생성, 조회, 수정, 삭제 및 사원 배정 API")
public class AdminDepartmentController {

    private final AdminDepartmentService adminDepartmentService;

    @Operation(summary = "부서 생성", description = "관리자가 신규 부서를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<AdminDepartmentResponseDTO>> createDepartment(
            @Valid @RequestBody DepartmentCreateRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(adminDepartmentService.createDepartment(request)));
    }

    @Operation(summary = "부서 목록 조회", description = "현재 사용 중인 부서 목록과 부서별 소속 인원 수를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminDepartmentResponseDTO>>> getDepartmentList() {
        return ResponseEntity.ok(ApiResponse.success(adminDepartmentService.getDepartmentList()));
    }

    @Operation(summary = "부서 상세 조회", description = "부서 코드로 부서 상세 정보를 조회합니다.")
    @GetMapping("/{deptCd}")
    public ResponseEntity<ApiResponse<AdminDepartmentResponseDTO>> getDepartment(@PathVariable String deptCd) {
        return ResponseEntity.ok(ApiResponse.success(adminDepartmentService.getDepartment(deptCd)));
    }

    @Operation(summary = "부서 수정", description = "부서명과 상위 부서를 수정합니다.")
    @PutMapping("/{deptCd}")
    public ResponseEntity<ApiResponse<AdminDepartmentResponseDTO>> updateDepartment(
            @PathVariable String deptCd,
            @Valid @RequestBody DepartmentUpdateRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(adminDepartmentService.updateDepartment(deptCd, request)));
    }

    @Operation(summary = "부서 삭제", description = "부서를 사용 중지 처리합니다. 소속 인원이 있으면 이동할 대체 부서가 필요합니다.")
    @DeleteMapping("/{deptCd}")
    public ResponseEntity<ApiResponse<String>> deleteDepartment(
            @PathVariable String deptCd,
            @RequestBody(required = false) DepartmentDeleteRequestDTO request) {
        adminDepartmentService.deleteDepartment(deptCd, request);
        return ResponseEntity.ok(ApiResponse.success("Department deleted."));
    }

    @Operation(summary = "부서 소속 사원 조회", description = "선택한 부서에 소속된 사원 목록을 조회합니다.")
    @GetMapping("/{deptCd}/members")
    public ResponseEntity<ApiResponse<List<AdminDepartmentMemberResponseDTO>>> getDepartmentMembers(
            @PathVariable String deptCd) {
        return ResponseEntity.ok(ApiResponse.success(adminDepartmentService.getDepartmentMembers(deptCd)));
    }

    @Operation(summary = "사원 부서 배정", description = "여러 사원을 선택한 부서에 배정합니다.")
    @PostMapping("/{deptCd}/members")
    public ResponseEntity<ApiResponse<DepartmentMemberMutationResponseDTO>> assignDepartmentMembers(
            @PathVariable String deptCd,
            @Valid @RequestBody DepartmentMemberAssignRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(adminDepartmentService.assignDepartmentMembers(deptCd, request)));
    }

    @Operation(summary = "사원 부서 이동", description = "선택한 부서의 여러 사원을 다른 부서로 이동합니다.")
    @PutMapping("/{deptCd}/members/transfer")
    public ResponseEntity<ApiResponse<DepartmentMemberMutationResponseDTO>> transferDepartmentMembers(
            @PathVariable String deptCd,
            @Valid @RequestBody DepartmentMemberTransferRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(adminDepartmentService.transferDepartmentMembers(deptCd, request)));
    }
}
