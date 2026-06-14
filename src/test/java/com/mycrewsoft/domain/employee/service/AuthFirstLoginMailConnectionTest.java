// package com.mycrewsoft.domain.employee.service;

// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.List;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.crypto.password.PasswordEncoder;

// import com.mycrewsoft.common.exception.CustomException;
// import com.mycrewsoft.common.exception.ErrorCode;
// import com.mycrewsoft.domain.employee.dto.request.FirstLoginRequestDTO;
// import com.mycrewsoft.domain.employee.mapper.AdminEmployeeMapper;
// import com.mycrewsoft.domain.employee.vo.EmployeeVO;
// import com.mycrewsoft.domain.empstat.code.EmpStatCode;
// import com.mycrewsoft.domain.mail.mapper.MailAccountMapper;
// import com.mycrewsoft.security.jwt.JwtTokenProvider;
// import com.mycrewsoft.security.rbac.AuthSessionFactory;
// import com.mycrewsoft.security.rbac.RbacSessionRefreshService;
// import com.mycrewsoft.security.service.AuthorizationUserDetailsService;
// import com.mycrewsoft.security.service.RefreshTokenService;
// import com.mycrewsoft.security.users.AuthorizationUserDetails;

// @ExtendWith(MockitoExtension.class)
// class AuthFirstLoginMailConnectionTest {

//     private static final Long EMP_ID = 20260001L;
//     private static final String EMAIL = "person@gmail.com";

//     @Mock
//     private AuthorizationUserDetailsService authorizationUserDetailsService;
//     @Mock
//     private JwtTokenProvider jwtTokenProvider;
//     @Mock
//     private RefreshTokenService refreshTokenService;
//     @Mock
//     private AuthSessionFactory authSessionFactory;
//     @Mock
//     private PasswordEncoder passwordEncoder;
//     @Mock
//     private RbacSessionRefreshService rbacSessionRefreshService;
//     @Mock
//     private AdminEmployeeMapper adminEmployeeMapper;
//     @Mock
//     private MailAccountMapper mailAccountMapper;

//     private AuthServiceImpl authService;

//     @BeforeEach
//     void setUp() {
//         authService = new AuthServiceImpl(
//                 authorizationUserDetailsService,
//                 jwtTokenProvider,
//                 refreshTokenService,
//                 authSessionFactory,
//                 passwordEncoder,
//                 rbacSessionRefreshService,
//                 adminEmployeeMapper,
//                 mailAccountMapper);
//         SecurityContextHolder.getContext().setAuthentication(
//                 new UsernamePasswordAuthenticationToken(
//                         currentUser(),
//                         null,
//                         currentUser().getAuthorities()));
//     }

//     @AfterEach
//     void clearSecurityContext() {
//         SecurityContextHolder.clearContext();
//     }

//     @Test
//     void firstLoginRejectsWhenGoogleMailAccountIsNotConnected() {
//         FirstLoginRequestDTO request = request();
//         when(adminEmployeeMapper.selectEmployeeById(EMP_ID)).thenReturn(initialEmployee());
//         when(mailAccountMapper.existsActiveGoogleMailAccountByEmail(EMP_ID, EMAIL)).thenReturn(0);

//         assertThatThrownBy(() -> authService.handleFirstLogin(request))
//                 .isInstanceOf(CustomException.class)
//                 .extracting("errorCode")
//                 .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

//         verify(adminEmployeeMapper, never()).updateFirstLoginInfo(
//                 eq(EMP_ID),
//                 org.mockito.ArgumentMatchers.anyString(),
//                 org.mockito.ArgumentMatchers.anyString());
//     }

//     @Test
//     void firstLoginUpdatesPasswordAndStatusWhenGoogleMailAccountIsConnected() {
//         FirstLoginRequestDTO request = request();
//         when(adminEmployeeMapper.selectEmployeeById(EMP_ID)).thenReturn(initialEmployee());
//         when(mailAccountMapper.existsActiveGoogleMailAccountByEmail(EMP_ID, EMAIL)).thenReturn(1);
//         when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encoded-password");

//         authService.handleFirstLogin(request);

//         verify(adminEmployeeMapper).updateFirstLoginInfo(
//                 EMP_ID,
//                 "encoded-password",
//                 EmpStatCode.EMP_LOGIN.getCode());
//     }

//     private FirstLoginRequestDTO request() {
//         FirstLoginRequestDTO request = new FirstLoginRequestDTO();
//         request.setEmailAddr(EMAIL);
//         request.setNewPassword("New-password1!");
//         return request;
//     }

//     private EmployeeVO initialEmployee() {
//         EmployeeVO employee = new EmployeeVO();
//         employee.setEmpId(EMP_ID);
//         employee.setEmpStatCd(EmpStatCode.EMP_INITIAL.getCode());
//         return employee;
//     }

//     private AuthorizationUserDetails currentUser() {
//         return new AuthorizationUserDetails(
//                 EMP_ID,
//                 String.valueOf(EMP_ID),
//                 null,
//                 true,
//                 EmpStatCode.EMP_INITIAL.getCode(),
//                 0,
//                 false,
//                 List.of(new SimpleGrantedAuthority("ROLE_USER")),
//                 List.of(),
//                 "session-1");
//     }
// }
