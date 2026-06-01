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
    public void save(String state, Long empId) {
        redisTemplate.opsForValue().set(key(state), String.valueOf(empId), STATE_TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public Long consume(String state) {
        String key = key(state);
        String value = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);

        if (value == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        return Long.valueOf(value);
    }

    private String key(String state) {
        return STATE_PREFIX + state;
    }
}
