package com.mycrewsoft.domain.employee.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.employee.dto.request.FirstLoginRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.LoginRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.TokenRefreshRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.LoginResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.TokenRefreshResponseDTO;
import com.mycrewsoft.domain.employee.service.AuthService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인, 로그아웃, 계정 찾기, 첫 로그인 시점을 처리하는 컨트롤러
 */
@Slf4j
@Tag(name = "Auth", description = "로그인, 로그아웃, 계정 찾기, 첫 로그인 처리 API")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
	        @Valid @RequestBody LoginRequestDTO request) {
		
		LoginResponseDTO response = authService.login(request);
	    return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
	}
	
	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<TokenRefreshResponseDTO>> refresh(
	        @Valid @RequestBody TokenRefreshRequestDTO request) {

	    TokenRefreshResponseDTO response =
	            authService.refreshToken(request);

	    return ResponseEntity.ok(
	            ApiResponse.success("토큰 재발급이 완료되었습니다.", response)
	    );
	}
	
	@PatchMapping("/first-login")
	public ResponseEntity<ApiResponse> handleFirstLogin(
	        @RequestBody FirstLoginRequestDTO request) {
		
		authService.handleFirstLogin(request);
	    
		return ResponseEntity.ok(ApiResponse.success("첫 로그인 처리 완료"));
	}
}
