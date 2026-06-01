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

import io.swagger.v3.oas.annotations.Operation;
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
	
	@Operation(summary = "로그인", description = "사용자의 로그인을 처리하는 API입니다.")
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
	        @Valid @RequestBody LoginRequestDTO request) {
		
		LoginResponseDTO response = authService.login(request);
	    return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
	}
	
	@Operation(summary = "토큰 재발급", description = "토큰 재발급 요청을 처리하는 API입니다.")
	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<TokenRefreshResponseDTO>> refresh(
	        @Valid @RequestBody TokenRefreshRequestDTO request) {

	    TokenRefreshResponseDTO response =
	            authService.refreshToken(request);

	    return ResponseEntity.ok(
	            ApiResponse.success("토큰 재발급이 완료되었습니다.", response)
	    );
	}
	
	@Operation(summary = "첫 로그인", description = "사용자가 처음 로그인할 때 처리하는 API입니다.")
	@PatchMapping("/first-login")
	public ResponseEntity<ApiResponse<String>> handleFirstLogin(
	        @RequestBody FirstLoginRequestDTO request) {
		
		authService.handleFirstLogin(request);
	    
		return ResponseEntity.ok(ApiResponse.success("첫 로그인 처리 완료"));
	}
	
	@Operation(summary = "로그아웃", description = "로그아웃을 진행하는 API입니다.")
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout() {
		authService.logout();
		
		return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
	}
}
