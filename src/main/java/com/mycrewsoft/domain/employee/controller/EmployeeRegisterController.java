package com.mycrewsoft.domain.employee.controller;

import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.request.EmployeeRegisterRequest;
import com.mycrewsoft.domain.employee.service.EmployeeService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class EmployeeRegisterController {
	private final EmployeeService employeeService;
	
	/**
	 * 관리자의 사원 등록 API 엔드포인트
	 * @param EmployeeRegisterRequest 객체
	 * @return ApiResponse 객체
	 */
	@PostMapping
	public ApiResponse registerEmployee(
			@Valid @RequestBody EmployeeRegisterRequest request) {		
		
		employeeService.registerEmployee(request);
		
		return ApiResponse.success("사원 등록이 완료되었습니다.");
	}
}
