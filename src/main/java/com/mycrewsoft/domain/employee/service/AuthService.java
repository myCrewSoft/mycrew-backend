package com.mycrewsoft.domain.employee.service;

import com.mycrewsoft.domain.employee.dto.request.LoginRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.LoginResponseDTO;

/**
 * 사용자의 인증 관련된 비즈니스 로직을 처리하는 서비스 인터페이스.
 */
public interface AuthService {
	/**
	 * @param request
	 * @return 로그인 요청에 대한 응답으로 액세스 토큰, 리프레시 토큰, 사번, 권한 버전, 첫 로그인 여부를 포함하는 DTO 객체
	 */
	LoginResponseDTO login(LoginRequestDTO request);
	
}
