package com.mycrewsoft.domain.employee.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mycrewsoft.domain.employee.dto.request.FirstLoginRequestDTO;
import com.mycrewsoft.domain.employee.dto.request.LoginRequestDTO;
import com.mycrewsoft.domain.employee.dto.response.LoginResponseDTO;
import com.mycrewsoft.domain.employee.mapper.AdminEmployeeMapper;
import com.mycrewsoft.domain.employee.vo.EmployeeVO;
import com.mycrewsoft.domain.empstat.code.EmpStatCode;
import com.mycrewsoft.security.jwt.JwtTokenProvider;
import com.mycrewsoft.security.rbac.AuthSessionFactory;
import com.mycrewsoft.security.rbac.RbacSessionRefreshService;
import com.mycrewsoft.security.service.AuthorizationUserDetailsService;
import com.mycrewsoft.security.service.RefreshTokenService;
import com.mycrewsoft.security.users.AuthSession;
import com.mycrewsoft.security.users.AuthorizationUserDetails;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthorizationUserDetailsService authorizationUserDetailsService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthSessionFactory authSessionFactory;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RbacSessionRefreshService rbacSessionRefreshService;

    @Mock
    private AdminEmployeeMapper adminEmployeeMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginDoesNotChangeEmployeeStatusWhenFirstLoginIsRequired() {
        LoginRequestDTO request = loginRequest();
        AuthorizationUserDetails userDetails = userDetails(EmpStatCode.EMP_INITIAL.getCode());
        AuthSession session = authSession(request.getEmpId());

        stubSuccessfulLogin(request, userDetails, session);

        LoginResponseDTO response = authService.login(request);

        assertThat(response.isFirstLoginRequired()).isTrue();
        verify(adminEmployeeMapper, never()).updateEmployeeStatus(any(), any());
        verify(refreshTokenService).saveSession(session, "refresh-token");
    }

    @Test
    void loginChangesEmployeeStatusToLoginWhenFirstLoginIsNotRequired() {
        LoginRequestDTO request = loginRequest();
        AuthorizationUserDetails userDetails = userDetails(EmpStatCode.EMP_LOGOUT.getCode());
        AuthSession session = authSession(request.getEmpId());

        stubSuccessfulLogin(request, userDetails, session);

        LoginResponseDTO response = authService.login(request);

        assertThat(response.isFirstLoginRequired()).isFalse();
        verify(adminEmployeeMapper).updateEmployeeStatus(
                request.getEmpId(),
                EmpStatCode.EMP_LOGIN.getCode());
        verify(refreshTokenService).saveSession(session, "refresh-token");
    }

    @Test
    void firstLoginChangesPasswordAndEmployeeStatusToLogin() {
        Long empId = 20260001L;
        FirstLoginRequestDTO request = new FirstLoginRequestDTO();
        request.setEmailAddr("employee@example.com");
        request.setNewPassword("NewPassword!123");

        EmployeeVO employee = new EmployeeVO();
        employee.setEmpId(empId);
        employee.setEmpStatCd(EmpStatCode.EMP_INITIAL.getCode());

        setCurrentUser(empId, "session-first-login");
        when(adminEmployeeMapper.selectEmployeeById(empId)).thenReturn(employee);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("{bcrypt}encoded");

        authService.handleFirstLogin(request);

        verify(adminEmployeeMapper).updateFirstLoginInfo(
                empId,
                "{bcrypt}encoded",
                EmpStatCode.EMP_LOGIN.getCode());
    }

    @Test
    void logoutChangesEmployeeStatusToLogoutAndDeletesRedisSession() {
        Long empId = 20260001L;
        setCurrentUser(empId, "session-logout");

        authService.logout();

        verify(adminEmployeeMapper).updateEmployeeStatusIfNotInitial(
                empId,
                EmpStatCode.EMP_LOGOUT.getCode());
        verify(refreshTokenService).deleteSession("session-logout");
    }

    private void stubSuccessfulLogin(
            LoginRequestDTO request,
            AuthorizationUserDetails userDetails,
            AuthSession session) {
        when(authorizationUserDetailsService.loadUserByEmpId(request.getEmpId()))
                .thenReturn(userDetails);
        when(passwordEncoder.matches(request.getPassword(), userDetails.getPassword()))
                .thenReturn(true);
        when(jwtTokenProvider.createAccessToken(eq(request.getEmpId()), eq(7), any(String.class)))
                .thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(eq(request.getEmpId()), eq(7), any(String.class)))
                .thenReturn("refresh-token");
        when(authSessionFactory.createSession(
                any(String.class),
                eq(request.getEmpId()),
                eq(String.valueOf(request.getEmpId())),
                eq(true),
                eq(7),
                eq(false),
                eq(new LinkedHashSet<>(Set.of("ROLE_USER"))),
                eq(null)))
                .thenReturn(session);
    }

    private LoginRequestDTO loginRequest() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmpId(20260001L);
        request.setPassword("20260001");
        return request;
    }

    private AuthorizationUserDetails userDetails(String empStat) {
        return new AuthorizationUserDetails(
                20260001L,
                "20260001",
                "{bcrypt}encoded",
                true,
                empStat,
                7,
                false,
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                List.of());
    }

    private AuthSession authSession(Long empId) {
        return new AuthSession(
                "session-id",
                empId,
                String.valueOf(empId),
                true,
                7,
                false,
                new LinkedHashSet<>(Set.of("ROLE_USER")),
                null);
    }

    private void setCurrentUser(Long empId, String sessionId) {
        AuthorizationUserDetails currentUser = new AuthorizationUserDetails(
                empId,
                String.valueOf(empId),
                null,
                true,
                EmpStatCode.EMP_LOGIN.getCode(),
                7,
                false,
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                List.of(),
                sessionId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        currentUser,
                        null,
                        currentUser.getAuthorities()));
    }
}
