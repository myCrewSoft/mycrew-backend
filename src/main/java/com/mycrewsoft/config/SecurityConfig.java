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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycrewsoft.common.constant.Constants;
import com.mycrewsoft.security.handler.CustomAccessDeniedHandler;
import com.mycrewsoft.security.handler.CustomAuthenticationEntryPoint;
import com.mycrewsoft.security.jwt.JwtAuthenticationFilter;
import com.mycrewsoft.security.jwt.JwtTokenProvider;
import com.mycrewsoft.security.service.AuthorizationUserDetailsService;
import com.mycrewsoft.security.service.RefreshTokenService;

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
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;
    
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
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint) // 401
                        .accessDeniedHandler(accessDeniedHandler) // 403
                )
                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, refreshTokenService, objectMapper),
                        UsernamePasswordAuthenticationFilter.class);
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
    
    /**
     * 사용자 인증 정보(권한버전, 권한, 세션ID)를 추가하여 로드하는 UserDetailsService 빈.
     * @return
     */
    @Bean
	public AuthorizationUserDetailsService  userDetailsService() {
		return new AuthorizationUserDetailsService();
	}
}
