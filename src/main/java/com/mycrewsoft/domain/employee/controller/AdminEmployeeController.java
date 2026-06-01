package com.mycrewsoft.domain.employee.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.request.EmployeeRegisterRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.EmployeeSearchDTO;
import com.mycrewsoft.domain.employee.dto.request.EmployeeStatusUpdateRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeDetailDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeListDTO;
import com.mycrewsoft.domain.employee.service.AdminEmployeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자의 사원 관리 API 컨트롤러.
 */
@Slf4j
@Tag(name = "Admin", description = "관리자 사원 관리 API")
@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminEmployeeController {

    private final AdminEmployeeService adminEmployeeService;

    @Operation(summary = "사원 등록", description = "관리자가 새로운 사원을 등록하는 API입니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<String>> registerEmployee(
            @Valid @RequestBody EmployeeRegisterRequestDTO request) {

        adminEmployeeService.registerEmployee(request);

        return ResponseEntity.ok(ApiResponse.success("사원 등록이 완료되었습니다."));
    }

    @Operation(summary = "사원 목록 조회", description = "관리자가 사원 목록을 조회하는 API입니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeListDTO>>> getEmployees(
            @ModelAttribute EmployeeSearchDTO condition) {

        Page<EmployeeListDTO> page = adminEmployeeService.getEmployees(condition);

        return ResponseEntity.ok(ApiResponse.success(page.getContent(), page));
    }

    @Operation(summary = "사원 상세 조회", description = "관리자가 사원의 상세 정보를 조회하는 API입니다.")
    @GetMapping("/{memberId}")
    public ApiResponse<EmployeeDetailDTO> getEmployee(
            @PathVariable("memberId") Long empId) {

        EmployeeDetailDTO response = adminEmployeeService.getEmployeeDetailById(empId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "사원 상태 변경", description = "관리자가 사원의 상태를 변경하는 API입니다.")
    @PutMapping("/{memberId}/status")
    public ApiResponse<String> updateEmployeeStatus(
            @PathVariable("memberId") Long empId,
            @Valid @RequestBody EmployeeStatusUpdateRequestDTO request) {
    	
        adminEmployeeService.updateEmployeeStatus(empId, request);
        return ApiResponse.success("사원 상태가 변경되었습니다.");
    }
}
