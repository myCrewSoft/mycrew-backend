package com.mycrewsoft.security.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 인증은 됐지만 권한이 없는 사용자가 리소스에 접근할 때 동작하는 핸들러.
 * 403 Forbidden 응답을 ApiResponse.fail 형태의 JSON 으로 반환한다.
 * SecurityConfig 의 exceptionHandling() 에 등록된다.
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiResponse<Void> body = ApiResponse.fail(
                ErrorCode.ACCESS_DENIED.getMessage(), ErrorCode.ACCESS_DENIED.getCode());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}