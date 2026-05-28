package com.mycrewsoft.domain.employee.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.employee.dto.request.FirstLoginRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.LoginRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.TokenRefreshRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.LoginResponseDTO;
import com.mycrewsoft.domain.employee.dto.response.TokenRefreshResponseDTO;
import com.mycrewsoft.domain.empstat.code.EmpStatCode;
import com.mycrewsoft.security.jwt.JwtTokenProvider;
import com.mycrewsoft.security.rbac.AuthSessionFactory;
import com.mycrewsoft.security.rbac.RbacSessionRefreshService;
import com.mycrewsoft.security.service.AuthorizationUserDetailsService;
import com.mycrewsoft.security.service.RefreshTokenService;
import com.mycrewsoft.security.users.AuthSession;
import com.mycrewsoft.security.users.AuthorizationUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
	private final AuthorizationUserDetailsService authorizationUserDetailsService;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final AuthSessionFactory authSessionFactory;
	private final PasswordEncoder passwordEncoder;
	private final RbacSessionRefreshService rbacSessionRefreshService;
	
	@Override
	@Transactional
	public LoginResponseDTO login(LoginRequestDTO request) {
		AuthorizationUserDetails userDetails;
		try {
			userDetails = authorizationUserDetailsService.loadUserByEmpId(request.getEmpId());
		} catch (UsernameNotFoundException e) {
			throw new CustomException(ErrorCode.LOGIN_FAILED);
		}	
		
		if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
		    throw new CustomException(ErrorCode.LOGIN_FAILED);
		}
		if (!userDetails.isEnabled()) {
		    throw new CustomException(ErrorCode.USER_DISABLED);
		}
		String empStat = userDetails.getEmpStat();
					
		if(empStat.equals(EmpStatCode.EMP_INACTIVE.getCode()) || empStat.equals(EmpStatCode.EMP_RETIRED.getCode())) {
			throw new CustomException(ErrorCode.USER_DISABLED);
		}
		
		
		String sessionId = UUID.randomUUID().toString();
		Integer authVersion = userDetails.getAuthVersion();

		String accessToken = jwtTokenProvider.createAccessToken(
		        userDetails.getEmpId(),
		        authVersion,
		        sessionId
		);

		String refreshToken = jwtTokenProvider.createRefreshToken(
		        userDetails.getEmpId(),
		        authVersion,
		        sessionId
		);

		Set<String> authorities = userDetails.getAuthorities().stream()
		        .map(GrantedAuthority::getAuthority)
		        .collect(Collectors.toCollection(LinkedHashSet::new));

		AuthSession session = authSessionFactory.createSession(
		        sessionId,
		        userDetails.getEmpId(),
		        userDetails.getUsername(),
		        userDetails.isEnabled(),
		        authVersion,
		        authorities,
		        null
		);

		refreshTokenService.saveSession(session, refreshToken);
		
		return LoginResponseDTO.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.empId(userDetails.getEmpId())
				.authVersion(authVersion)
				.firstLoginRequired(EmpStatCode.EMP_INITIAL.getCode().equals(userDetails.getEmpStat()))
				.build();
	}

	@Override
	@Transactional
	public TokenRefreshResponseDTO refreshToken(TokenRefreshRequestDTO request) {
	    String refreshToken = request.getRefreshToken();

	    if (!jwtTokenProvider.validateToken(refreshToken)) {
	        throw new CustomException(ErrorCode.INVALID_TOKEN);
	    }

	    if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
	        throw new CustomException(ErrorCode.INVALID_TOKEN);
	    }

	    Long empId = jwtTokenProvider.getEmpId(refreshToken);
	    String sessionId = jwtTokenProvider.getSessionId(refreshToken);

	    AuthSession session = refreshTokenService.getSessionOrThrow(sessionId);

	    if (!session.getEmpId().equals(empId)) {
	        throw new CustomException(ErrorCode.INVALID_TOKEN);
	    }

	    if (!refreshTokenService.isValidRefreshToken(sessionId, refreshToken)) {
	        throw new CustomException(ErrorCode.INVALID_TOKEN);
	    }

	    AuthSession latestSession =
	            rbacSessionRefreshService.refreshIfStale(session);

	    String newAccessToken = jwtTokenProvider.createAccessToken(
	            latestSession.getEmpId(),
	            latestSession.getAuthVersion(),
	            latestSession.getSessionId()
	    );

	    String newRefreshToken = jwtTokenProvider.createRefreshToken(
	            latestSession.getEmpId(),
	            latestSession.getAuthVersion(),
	            latestSession.getSessionId()
	    );

	    refreshTokenService.rotateRefreshToken(
	            latestSession.getSessionId(),
	            newRefreshToken
	    );

	    return TokenRefreshResponseDTO.builder()
	            .accessToken(newAccessToken)
	            .refreshToken(newRefreshToken)
	            .empId(latestSession.getEmpId())
	            .authVersion(latestSession.getAuthVersion())
	            .build();
	}

	@Override
	public void handleFirstLogin(FirstLoginRequestDTO request) {
		// TODO Auto-generated method stub
		
	}

}
