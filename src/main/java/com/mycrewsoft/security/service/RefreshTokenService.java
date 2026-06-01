package com.mycrewsoft.security.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.rbac.AuthSessionStore;
import com.mycrewsoft.security.users.AuthSession;

import lombok.RequiredArgsConstructor;

/**
 * Redis 기반 인증 세션 저장 서비스.
 *
 * 역할:
 * - 로그인 세션을 Redis에 저장한다.
 * - 세션 ID로 AuthSession을 조회한다.
 * - 권한 정보가 갱신된 AuthSession을 Redis에 다시 저장한다.
 * - 사용자별 세션 인덱스를 관리한다.
 *
 * 사용 목적:
 * - JWT 기반 stateless 인증에서 서버 측 권한 상태를 최신으로 유지한다.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements AuthSessionStore {

    private static final String SESSION_PREFIX = "auth:session:";
    private static final String EMP_SESSIONS_PREFIX = "auth:emp_sessions:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public void saveSession(AuthSession session, String refreshToken) {
        session.setRefreshTokenHash(hash(refreshToken));
        redisTemplate.opsForValue().set(
                key(session.getSessionId()),
                write(session),
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS);
        indexSession(session);
    }

    public Optional<AuthSession> findSession(String sessionId) {
        String value = redisTemplate.opsForValue().get(key(sessionId));

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(read(value));
    }

    public AuthSession getSessionOrThrow(String sessionId) {
        return findSession(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));
    }

    public boolean isValidRefreshToken(String sessionId, String refreshToken) {
        return findSession(sessionId)
                .map(session -> hash(refreshToken).equals(session.getRefreshTokenHash()))
                .orElse(false);
    }

    public void rotateRefreshToken(String sessionId, String refreshToken) {
        AuthSession session = getSessionOrThrow(sessionId);
        saveSession(session, refreshToken);
    }

    @Override
    public void updateSession(AuthSession session) {
        redisTemplate.opsForValue().set(
                key(session.getSessionId()),
                write(session),
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS);
        indexSession(session);
    }

    public void deleteSession(String sessionId) {
        findSession(sessionId).ifPresent(session ->
                redisTemplate.opsForSet().remove(empSessionsKey(session.getEmpId()), sessionId));
        redisTemplate.delete(key(sessionId));
    }

    public void deleteSessionsByEmpId(Long empId) {
        if (empId == null) {
            return;
        }

        String empSessionsKey = empSessionsKey(empId);
        Set<String> sessionIds = redisTemplate.opsForSet().members(empSessionsKey);
        if (sessionIds != null) {
            sessionIds.forEach(sessionId -> redisTemplate.delete(key(sessionId)));
        }
        redisTemplate.delete(empSessionsKey);
    }

    private String key(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    private String empSessionsKey(Long empId) {
        return EMP_SESSIONS_PREFIX + empId;
    }

    private void indexSession(AuthSession session) {
        redisTemplate.opsForSet().add(empSessionsKey(session.getEmpId()), session.getSessionId());
        redisTemplate.expire(empSessionsKey(session.getEmpId()), refreshTokenExpiration, TimeUnit.MILLISECONDS);
    }

    private String write(AuthSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private AuthSession read(String value) {
        try {
            return objectMapper.readValue(value, AuthSession.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
