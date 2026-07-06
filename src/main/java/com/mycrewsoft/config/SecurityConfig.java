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
import com.mycrewsoft.security.rbac.RbacSessionRefreshService;
import com.mycrewsoft.security.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

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
    private final RbacSessionRefreshService rbacSessionRefreshService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(Constants.PUBLIC_URLS).permitAll()
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
