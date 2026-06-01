package com.mycrewsoft.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.mycrewsoft.common.constant.Constants;
import com.mycrewsoft.security.handler.CustomAccessDeniedHandler;
import com.mycrewsoft.security.handler.CustomAuthenticationEntryPoint;
import com.mycrewsoft.security.jwt.JwtAuthenticationFilter;
import com.mycrewsoft.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security 의 핵심 설정 클래스.
 * JWT 기반 Stateless 인증, 권한별 URL 접근 제어, 예외 처리 핸들러를 설정한다.
 *
 * @EnableMethodSecurity: @PreAuthorize 어노테이션을 활성화한다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Security 필터 체인을 구성한다.
     * CSRF 비활성화, 세션 미사용, URL 권한 설정, JWT 필터 등록을 처리한다.
     *
     * @param http HttpSecurity 빌더
     * @return 구성된 SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // cors 적용
                .csrf(csrf -> csrf.disable()) // csrf 미사용
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 서버 세션 미사용
                                                                                                  // (Stateless)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(Constants.PUBLIC_URLS).permitAll()
                        // 역할별 URL 제한은 팀과 역할 이름 확정 후 여기에 추가
                        // 예) .requestMatchers("/api/v1/admin/**").hasRole("GOD")
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(new JwtAuthenticationFilter(
                                jwtTokenProvider,
                                refreshTokenService,
                                objectMapper,
                                rbacSessionRefreshService),
                        UsernamePasswordAuthenticationFilter.class)
                // SSE 설정
                .headers(headers -> headers
                    .frameOptions(frame -> frame.disable())
                );
        return http.build();
    }

    /**
     * 저장된 비밀번호의 prefix({bcrypt}, {noop} 등)를 기반으로
     * 적절한 PasswordEncoder를 선택하여 동작한다.
     *
     * @return passwordEncoder 구현체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}