package com.mycrewsoft.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycrewsoft.app.common.exception.ErrorCode;
import com.mycrewsoft.app.common.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증되지 않은 사용자가 보호된 리소스에 접근할 때 동작하는 핸들러.
 * 401 Unauthorized 응답을 ApiResponse.fail 형태의 JSON 으로 반환한다.
 * SecurityConfig 의 exceptionHandling() 에 등록된다.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiResponse<Void> body = ApiResponse.fail(
                ErrorCode.UNAUTHORIZED.getMessage(), ErrorCode.UNAUTHORIZED.getCode());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}