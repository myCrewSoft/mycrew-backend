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
	 * 로그인 요청에 대한 응답으로 액세스 토큰, 리프레시 토큰, 사번, 권한 버전, 첫 로그인 여부를 포함하는 DTO 객체
	 * @param LoginRequestDTO request
	 * @return LoginResponseDTO
	 */
	LoginResponseDTO login(LoginRequestDTO request);
	
	/**
	 * 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급하는 메서드
	 * @param TokenRefreshRequestDTO refreshToken
	 * @return TokenRefreshResponseDTO
	 */
	TokenRefreshResponseDTO refreshToken(TokenRefreshRequestDTO request);
	
	/**
	 * 첫 로그인 시점에 필요한 처리를 수행하는 메서드.
	 * @param FirstLoginRequestDTO request
	 */
	void handleFirstLogin(FirstLoginRequestDTO request);

	/**
	 * 로그아웃을 처리하는 메서드. 현재 로그인한 사용자의 세션을 무효화하고 관련된 토큰을 삭제하는 등의 작업을 수행한다.
	 */
	void logout();
}
