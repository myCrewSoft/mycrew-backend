//package com.mycrewsoft.security.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.tuple;
//
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Assumptions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
//import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.data.redis.connection.RedisConnection;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.mock.web.MockFilterChain;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mycrewsoft.config.RedisConfig;
//import com.mycrewsoft.security.authz.ScopeType;
//import com.mycrewsoft.security.authz.ScopedPermission;
//import com.mycrewsoft.security.jwt.JwtAuthenticationFilter;
//import com.mycrewsoft.security.jwt.JwtTokenProvider;
//import com.mycrewsoft.security.users.AuthSession;
//import com.mycrewsoft.security.users.AuthorizationUserDetails;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@SpringJUnitConfig(classes = {
//        RedisConfig.class,
//        RefreshTokenService.class,
//        RefreshTokenServiceRedisIntegrationTest.ObjectMapperTestConfig.class
//})
//@ImportAutoConfiguration(RedisAutoConfiguration.class)
//@TestPropertySource(properties = {
//        "spring.data.redis.host=localhost",
//        "spring.data.redis.port=6379",
//        "spring.data.redis.timeout=3000",
//        "jwt.refresh-token-expiration=600000"
//})
//class RefreshTokenServiceRedisIntegrationTest {
//
//    private static final String SESSION_PREFIX = "auth:session:";
//    private static final String JWT_SECRET = "12345678901234567890123456789012";
//
//    @Autowired
//    private RefreshTokenService refreshTokenService;
//
//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private String sessionId;
//
//    @BeforeEach
//    void assumeRedisIsAvailable() {
//        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
//            connection.ping();
//        } catch (RuntimeException e) {
//            Assumptions.abort("localhost:6379 Redis is not available. Start Redis before running this integration test.");
//        }
//    }
//
//    @AfterEach
//    void cleanUp() {
//        SecurityContextHolder.clearContext();
//
//        if (sessionId != null) {
//            redisTemplate.delete(key(sessionId));
//        }
//    }
//
//    @Test
//    void createsRedisSessionWithInjectedTokenData() {
//        sessionId = "test-session-" + UUID.randomUUID();
//        String refreshToken = "test-refresh-token-" + UUID.randomUUID();
//        AuthSession session = new AuthSession(
//                sessionId,
//                1004L,
//                "redis-test-user",
//                true,
//                7,
//                new LinkedHashSet<>(Set.of("ROLE_USER", "ROLE_ADMIN")),
//                null);
//
//        refreshTokenService.saveSession(session, refreshToken);
//
//        String redisKey = key(sessionId);
//        String rawSessionJson = redisTemplate.opsForValue().get(redisKey);
//        Long ttlMillis = redisTemplate.getExpire(redisKey, TimeUnit.MILLISECONDS);
//        Optional<AuthSession> foundSession = refreshTokenService.findSession(sessionId);
//
//        assertThat(rawSessionJson).isNotBlank();
//        assertThat(ttlMillis).isNotNull().isPositive();
//        assertThat(foundSession).isPresent();
//        assertThat(foundSession.get().getSessionId()).isEqualTo(sessionId);
//        assertThat(foundSession.get().getEmpId()).isEqualTo(1004L);
//        assertThat(foundSession.get().getUsername()).isEqualTo("redis-test-user");
//        assertThat(foundSession.get().getAuthVersion()).isEqualTo(7);
//        assertThat(foundSession.get().getAuthorities()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
//        assertThat(foundSession.get().getRefreshTokenHash()).isNotBlank();
//        assertThat(refreshTokenService.isValidRefreshToken(sessionId, refreshToken)).isTrue();
//
//        log.info("Redis Session Key: {}", redisKey);
//        log.info("Injected Refresh Token: {}", refreshToken);
//        log.info("Stored Raw Session JSON: {}", rawSessionJson);
//        log.info("Redis Session TTL(ms): {}", ttlMillis);
//        log.info("Loaded AuthSession: sessionId={}, empId={}, username={}, authVersion={}, authorities={}, refreshTokenHash={}",
//                foundSession.get().getSessionId(),
//                foundSession.get().getEmpId(),
//                foundSession.get().getUsername(),
//                foundSession.get().getAuthVersion(),
//                foundSession.get().getAuthorities(),
//                foundSession.get().getRefreshTokenHash());
//    }
//
//    @Test
//    void createsAuthenticationWithAuthoritiesLoadedFromRedisSession() throws Exception {
//        sessionId = "test-auth-session-" + UUID.randomUUID();
//        AuthSession session = new AuthSession(
//                sessionId,
//                2001L,
//                "authentication-test-user",
//                true,
//                11,
//                new LinkedHashSet<>(Set.of("ROLE_USER", "ROLE_MANAGER")),
//                null);
//        session.setScopedPermissions(List.of(
//                ScopedPermission.of("BOARD_DELETE", 10L, "board manager", ScopeType.DEPT, "10")));
//        String refreshToken = "test-refresh-token-" + UUID.randomUUID();
//        refreshTokenService.saveSession(session, refreshToken);
//
//        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(JWT_SECRET, 600000L, 600000L);
//        String accessToken = jwtTokenProvider.createAccessToken(session.getEmpId(), session.getAuthVersion(), sessionId);
//        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider, refreshTokenService, objectMapper);
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        MockFilterChain filterChain = new MockFilterChain();
//        request.addHeader("Authorization", "Bearer " + accessToken);
//
//        filter.doFilter(request, response, filterChain);
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        assertThat(response.getStatus()).isEqualTo(200);
//        assertThat(authentication).isNotNull();
//        assertThat(authentication.isAuthenticated()).isTrue();
//        assertThat(authentication.getPrincipal()).isInstanceOf(AuthorizationUserDetails.class);
//        assertThat(authentication.getAuthorities())
//                .extracting("authority")
//                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_MANAGER");
//
//        AuthorizationUserDetails principal = (AuthorizationUserDetails) authentication.getPrincipal();
//        assertThat(principal.getEmpId()).isEqualTo(2001L);
//        assertThat(principal.getUsername()).isEqualTo("authentication-test-user");
//        assertThat(principal.getSessionId()).isEqualTo(sessionId);
//        assertThat(principal.getAuthVersion()).isEqualTo(11);
//        assertThat(principal.getScopedPermissions())
//                .extracting("permCd", "scopeType", "scopeId")
//                .containsExactly(tuple("BOARD_DELETE", ScopeType.DEPT, "10"));
//
//        log.info("Authentication Principal: empId={}, username={}, authVersion={}",
//                principal.getEmpId(),
//                principal.getUsername(),
//                principal.getAuthVersion());
//        log.info("Authentication Authorities from Redis Session: {}", authentication.getAuthorities());
//        log.info("Access Token Session ID: {}", jwtTokenProvider.getSessionId(accessToken));
//    }
//
//    private String key(String sessionId) {
//        return SESSION_PREFIX + sessionId;
//    }
//
//    @TestConfiguration
//    static class ObjectMapperTestConfig {
//
//        @Bean
//        ObjectMapper objectMapper() {
//            return new ObjectMapper();
//        }
//    }
//}
