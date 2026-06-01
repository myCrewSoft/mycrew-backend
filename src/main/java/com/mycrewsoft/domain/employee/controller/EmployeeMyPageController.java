package com.mycrewsoft.domain.employee.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.request.LoginRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.employee.dto.response.LoginResponseDTO;
import com.mycrewsoft.domain.employee.service.AuthService;
import com.mycrewsoft.domain.employee.service.MyPageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "직원 마이페이지 API")
public class EmployeeMyPageController {
	private final MyPageService myPageService;
	
	/**
	 * 사용자의 프로필 정보를 반환하는 API(헤더 표시용)
	 * @return EmployeeProfileDTO
	 */
	@Operation(summary = "프로필 정보", description = "사용자의 간략한 정보를 담은 프로필을 반환하는 API입니다.")
	@GetMapping("/profile")
	public ResponseEntity<ApiResponse<EmployeeProfileDTO>> profile() {
		
		EmployeeProfileDTO response = myPageService.getEmployeeProfile();
		
	    return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
	}
}
