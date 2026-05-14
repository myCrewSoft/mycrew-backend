package com.mycrewsoft.app.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * 모든 HTTP 요청에서 JWT 토큰을 추출하고 검증하는 필터.
 * OncePerRequestFilter 를 상속하여 요청당 정확히 한 번만 실행된다.
 * Spring Security 필터 체인에서 UsernamePasswordAuthenticationFilter 앞에 등록된다.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 요청 헤더에서 토큰을 추출하고 유효성을 검증한 후 SecurityContext 에 인증 정보를 저장한다.
     * 토큰이 없거나 유효하지 않으면 SecurityContext 에 저장하지 않고 다음 필터로 넘긴다.
     * 인증 실패 처리는 CustomAuthenticationEntryPoint 가 담당한다.
     *
     * @param request     HTTP 요청
     * @param response    HTTP 응답
     * @param filterChain 다음 필터 체인
     */
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("인증 처리 완료 - 사용자: {}", auth.getName());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청의 Authorization 헤더에서 Bearer 토큰을 추출한다.
     * "Bearer " 접두사를 제거하고 순수 토큰 문자열만 반환한다.
     *
     * @param request HTTP 요청
     * @return 추출된 토큰 문자열. Authorization 헤더가 없거나 형식이 맞지 않으면 null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX))
            return bearerToken.substring(BEARER_PREFIX.length());

        return null;
    }
}