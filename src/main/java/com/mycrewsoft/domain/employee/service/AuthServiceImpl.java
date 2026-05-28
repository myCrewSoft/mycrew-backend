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
import com.mycrewsoft.domain.employee.dto.request.LoginRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.LoginResponseDTO;
import com.mycrewsoft.domain.empstat.code.EmpStatCode;
import com.mycrewsoft.security.jwt.JwtTokenProvider;
import com.mycrewsoft.security.rbac.AuthSessionFactory;
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

}
