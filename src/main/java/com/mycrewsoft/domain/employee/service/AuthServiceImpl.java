package com.mycrewsoft.domain.employee.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
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
import com.mycrewsoft.domain.employee.event.FirstLoginEvent;
import com.mycrewsoft.domain.employee.mapper.AdminEmployeeMapper;
import com.mycrewsoft.domain.employee.vo.EmployeeVO;
import com.mycrewsoft.domain.empstat.code.EmpStatCode;
import com.mycrewsoft.domain.mail.mapper.MailAccountMapper;
import com.mycrewsoft.security.jwt.JwtTokenProvider;
import com.mycrewsoft.security.rbac.AuthSessionFactory;
import com.mycrewsoft.security.rbac.RbacSessionRefreshService;
import com.mycrewsoft.security.service.AuthorizationUserDetailsService;
import com.mycrewsoft.security.service.RefreshTokenService;
import com.mycrewsoft.security.util.SecurityUtil;
import com.mycrewsoft.security.users.AuthSession;
import com.mycrewsoft.security.users.AuthorizationUserDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
	private final AuthorizationUserDetailsService authorizationUserDetailsService;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final AuthSessionFactory authSessionFactory;
	private final PasswordEncoder passwordEncoder;
	private final RbacSessionRefreshService rbacSessionRefreshService;
	private final AdminEmployeeMapper adminEmployeeMapper;
	private final MailAccountMapper mailAccountMapper;
	private final ApplicationEventPublisher eventPublisher;
	
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
					
		if(EmpStatCode.EMP_INACTIVE.getCode().equals(empStat) || EmpStatCode.EMP_RETIRED.getCode().equals(empStat)) {
			throw new CustomException(ErrorCode.USER_DISABLED);
		}

		boolean firstLoginRequired = EmpStatCode.EMP_INITIAL.getCode().equals(empStat);
		if (!firstLoginRequired) {
			adminEmployeeMapper.updateEmployeeStatus(
					userDetails.getEmpId(),
					EmpStatCode.EMP_LOGIN.getCode());
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
		        userDetails.isExec(),
		        authorities,
		        null
		);

		refreshTokenService.saveSession(session, refreshToken);
		
		return LoginResponseDTO.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.empId(userDetails.getEmpId())
				.authVersion(authVersion)
				.firstLoginRequired(firstLoginRequired)
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
	@Transactional
	public void handleFirstLogin(FirstLoginRequestDTO request) {
		Long empId = SecurityUtil.getCurrentEmpId();
		EmployeeVO employee = adminEmployeeMapper.selectEmployeeById(empId);

		if (employee == null) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND);
		}
		if (!EmpStatCode.EMP_INITIAL.getCode().equals(employee.getEmpStatCd())) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
		}
		if (!StringUtils.hasText(request.getEmailAddr())
				|| mailAccountMapper.existsActiveGoogleMailAccountByEmail(empId, request.getEmailAddr()) < 1) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
		}

		String encodedPassword = passwordEncoder.encode(request.getNewPassword());
		adminEmployeeMapper.updateFirstLoginInfo(
				empId,
				encodedPassword,
				EmpStatCode.EMP_LOGIN.getCode());
		
		// 첫로그인시 입사 알림
		eventPublisher.publishEvent(
			new FirstLoginEvent(empId)
		);
	}

	@Override
	@Transactional
	public void logout() {
		AuthorizationUserDetails currentUser = SecurityUtil.getCurrentUser();
		String sessionId = currentUser.getSessionId();

		if (!StringUtils.hasText(sessionId)) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}

		adminEmployeeMapper.updateEmployeeStatusIfNotInitial(
				currentUser.getEmpId(),
				EmpStatCode.EMP_LOGOUT.getCode());

		refreshTokenService.deleteSession(sessionId);
	}

}
