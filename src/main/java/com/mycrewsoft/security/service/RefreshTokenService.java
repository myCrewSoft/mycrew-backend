package com.mycrewsoft.security.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.users.AuthSession;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String SESSION_PREFIX = "auth:session:";

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

    public void updateSession(AuthSession session) {
        redisTemplate.opsForValue().set(
                key(session.getSessionId()),
                write(session),
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS);
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(key(sessionId));
    }

    private String key(String sessionId) {
        return SESSION_PREFIX + sessionId;
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
