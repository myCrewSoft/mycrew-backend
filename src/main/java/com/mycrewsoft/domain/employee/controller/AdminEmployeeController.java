package com.mycrewsoft.domain.employee.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.request.EmployeeRegisterRequestDTO;
import com.mycrewsoft.domain.employee.service.AdminEmployeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자용 사원 관리 컨트롤러
 */
@Slf4j
@Tag(name = "Admin", description = "관리자의 사원 관리 API")
@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminEmployeeController {
	private final AdminEmployeeService employeeService;
	
	/**
	 * 관리자의 사원 등록 API 엔드포인트
	 * @param EmployeeRegisterRequestDTO 객체
	 * @return ApiResponse 객체
	 */
	@Operation(summary = "사원 등록", description = "관리자가 새로운 사원을 등록하는 API입니다.")
	@PostMapping
	public ResponseEntity<ApiResponse<String>> registerEmployee(
			@Valid @RequestBody EmployeeRegisterRequestDTO request) {		
		
		employeeService.registerEmployee(request);
		
		return ResponseEntity.ok(ApiResponse.success("사원 등록이 완료되었습니다."));
	}
}
