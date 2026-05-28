package com.mycrewsoft.domain.employee.service;

import com.mycrewsoft.domain.employee.dto.request.FirstLoginRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.LoginRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.TokenRefreshRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.LoginResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.TokenRefreshResponseDTO;

/**
 * 사용자의 인증 관련된 비즈니스 로직을 처리하는 서비스 인터페이스.
 */
public interface AuthService {
	/**
	 * @param request
	 * @return 로그인 요청에 대한 응답으로 액세스 토큰, 리프레시 토큰, 사번, 권한 버전, 첫 로그인 여부를 포함하는 DTO 객체
	 */
	LoginResponseDTO login(LoginRequestDTO request);
	
	/**
	 * @param refreshToken
	 * @return 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급하는 메서드
	 */
	TokenRefreshResponseDTO refreshToken(TokenRefreshRequestDTO request);
	
	/**
	 * @param request
	 * @return 첫 로그인 시점에 필요한 처리를 수행하는 메서드.
	 */
	void handleFirstLogin(FirstLoginRequestDTO request);

	void logout();
}
