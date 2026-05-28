package com.mycrewsoft.domain.employee.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginCreatesTokensAndStoresRedisSessionWithScopedPermissionsSource() {
        LoginRequestDTO request = loginRequest();
        AuthorizationUserDetails userDetails = userDetails(true, EmpStatCode.EMP_INITIAL.getCode());
        AuthSession session = new AuthSession(
                "session-1",
                request.getEmpId(),
                String.valueOf(request.getEmpId()),
                true,
                7,
                new LinkedHashSet<>(Set.of("ROLE_USER")),
                null);

        when(authorizationUserDetailsService.loadUserByEmpId(request.getEmpId())).thenReturn(userDetails);
        when(passwordEncoder.matches(request.getPassword(), userDetails.getPassword())).thenReturn(true);
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
                eq(new LinkedHashSet<>(Set.of("ROLE_USER"))),
                eq(null)))
                .thenReturn(session);

        LoginResponseDTO response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getEmpId()).isEqualTo(request.getEmpId());
        assertThat(response.getAuthVersion()).isEqualTo(7);
        assertThat(response.isFirstLoginRequired()).isTrue();

        ArgumentCaptor<String> refreshTokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenService).saveSession(eq(session), refreshTokenCaptor.capture());
        assertThat(refreshTokenCaptor.getValue()).isEqualTo("refresh-token");
    }

    @Test
    void loginRejectsInvalidPassword() {
        LoginRequestDTO request = loginRequest();
        AuthorizationUserDetails userDetails = userDetails(true, EmpStatCode.EMP_ACTIVE.getCode());

        when(authorizationUserDetailsService.loadUserByEmpId(request.getEmpId())).thenReturn(userDetails);
        when(passwordEncoder.matches(request.getPassword(), userDetails.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_FAILED);

        verify(refreshTokenService, never()).saveSession(any(), any());
    }

    @Test
    void logoutDeletesCurrentRedisSession() {
        AuthorizationUserDetails currentUser = new AuthorizationUserDetails(
                20260001L,
                "20260001",
                null,
                true,
                EmpStatCode.EMP_ACTIVE.getCode(),
                7,
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                List.of(),
                "session-logout-1");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        currentUser,
                        null,
                        currentUser.getAuthorities()));

        authService.logout();

        verify(refreshTokenService).deleteSession("session-logout-1");
    }

    private LoginRequestDTO loginRequest() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmpId(20260001L);
        request.setPassword("20260001");
        return request;
    }

    private AuthorizationUserDetails userDetails(boolean enabled, String empStat) {
        return new AuthorizationUserDetails(
                20260001L,
                "20260001",
                "{bcrypt}encoded",
                enabled,
                empStat,
                7,
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                List.of());
    }
}
