package com.mycrewsoft.security.jwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.security.service.RefreshTokenService;
import com.mycrewsoft.security.users.AuthSession;
import com.mycrewsoft.security.users.AuthorizationUserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        try {
            if (token != null && jwtTokenProvider.validateAccessToken(token)) {
                Long userId = jwtTokenProvider.getUserId(token);
                String sessionId = jwtTokenProvider.getSessionId(token);
                Integer tokenAuthVersion = jwtTokenProvider.getAuthVersion(token);
                AuthSession session = refreshTokenService.getSessionOrThrow(sessionId);

                if (!userId.equals(session.getUserId())) {
                    throw new CustomException(ErrorCode.INVALID_TOKEN);
                }
                if (!tokenAuthVersion.equals(session.getAuthVersion())) {
                    throw new CustomException(ErrorCode.AUTH_VERSION_MISMATCH);
                }
                if (!session.isEnabled()) {
                	throw new CustomException(ErrorCode.USER_DISABLED);
                }
                
                AuthorizationUserDetails userDetails = createUserDetails(session);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication completed - userId: {}", userId);
            }
        } catch (CustomException e) {
            writeErrorResponse(response, e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    private AuthorizationUserDetails createUserDetails(AuthSession session) {
        List<SimpleGrantedAuthority> authorities = session.getAuthorities().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new AuthorizationUserDetails(
                session.getUserId(),
                session.getUsername(),
                null,
                session.isEnabled(),
                session.getAuthVersion(),
                authorities,
                session.getScopedPermissions());
    }

    private void writeErrorResponse(HttpServletResponse response, CustomException exception) throws IOException {
        ErrorCode errorCode = exception.getErrorCode();

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.fail(errorCode.getMessage(), errorCode.getCode())));
    }
}
