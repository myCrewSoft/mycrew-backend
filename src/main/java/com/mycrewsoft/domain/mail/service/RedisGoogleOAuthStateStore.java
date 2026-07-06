package com.mycrewsoft.domain.mail.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisGoogleOAuthStateStore implements GoogleOAuthStateStore {

    private static final String STATE_PREFIX = "mail:google_oauth:state:";
    private static final long STATE_TTL_MINUTES = 10L;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(String state, Long empId, String context, String emailAddr) {
        redisTemplate.opsForValue().set(key(state), value(empId, context, emailAddr), STATE_TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public GoogleOAuthState consume(String state) {
        String key = key(state);
        String value = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);

        if (value == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String[] parts = value.split("\\|", 3);
        String context = parts.length >= 2 && !parts[1].isBlank() ? parts[1] : null;
        String emailAddr = parts.length == 3 && !parts[2].isBlank() ? parts[2] : null;
        return new GoogleOAuthState(Long.valueOf(parts[0]), context, emailAddr);
    }

    private String key(String state) {
        return STATE_PREFIX + state;
    }

    private String value(Long empId, String context, String emailAddr) {
        if (context == null || context.isBlank()) {
            return String.valueOf(empId);
        }
        if (emailAddr == null || emailAddr.isBlank()) {
            return empId + "|" + context.trim();
        }
        return empId + "|" + context.trim() + "|" + emailAddr.trim();
    }
}
