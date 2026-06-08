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
    public void save(String state, Long empId, String context) {
        redisTemplate.opsForValue().set(key(state), value(empId, context), STATE_TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public GoogleOAuthState consume(String state) {
        String key = key(state);
        String value = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);

        if (value == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String[] parts = value.split("\\|", 2);
        String context = parts.length == 2 && !parts[1].isBlank() ? parts[1] : null;
        return new GoogleOAuthState(Long.valueOf(parts[0]), context);
    }

    private String key(String state) {
        return STATE_PREFIX + state;
    }

    private String value(Long empId, String context) {
        if (context == null || context.isBlank()) {
            return String.valueOf(empId);
        }
        return empId + "|" + context.trim();
    }
}
