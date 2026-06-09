package com.mycrewsoft.domain.employee.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mycrewsoft.domain.employee.dto.request.ChangeEmailRequestDTO;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.domain.mail.dto.response.GoogleOAuthAuthorizeResponse;
import com.mycrewsoft.domain.mail.service.GoogleOAuthService;
import com.mycrewsoft.security.users.AuthorizationUserDetails;

@ExtendWith(MockitoExtension.class)
class MyPageServiceImplTest {

    private static final Long EMP_ID = 1001L;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GoogleOAuthService googleOAuthService;

    private MyPageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MyPageServiceImpl(employeeMapper, passwordEncoder, googleOAuthService);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new AuthorizationUserDetails(EMP_ID, "user", "", true, 1, false, List.of(), List.of()),
                        null,
                        List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void changeEmailStartsGoogleOAuthForRequestedEmail() {
        ChangeEmailRequestDTO request = new ChangeEmailRequestDTO();
        request.setEmailAddr("new.person@gmail.com");
        GoogleOAuthAuthorizeResponse authorizeResponse =
                new GoogleOAuthAuthorizeResponse("https://accounts.google.com/o/oauth2/v2/auth?state=state-1");
        when(googleOAuthService.createAuthorizationUrl(EMP_ID, "mypage-email", "new.person@gmail.com"))
                .thenReturn(authorizeResponse);

        GoogleOAuthAuthorizeResponse response = service.changeEmail(request);

        assertThat(response).isSameAs(authorizeResponse);
    }
}
