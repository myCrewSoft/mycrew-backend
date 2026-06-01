package com.mycrewsoft.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class JwtTokenProviderTest {

    @Test
    void accessTokenContainsUserIdAuthVersionAndAccessType() throws Exception {
        Object jwtTokenProvider = newJwtTokenProvider();
        String token = invoke(jwtTokenProvider, "createAccessToken", 10L, 3, "session-1");

        assertThat((Long) invoke(jwtTokenProvider, "getEmpId", token)).isEqualTo(10L);
        assertThat((String) invoke(jwtTokenProvider, "getSessionId", token)).isEqualTo("session-1");
        assertThat((Integer) invoke(jwtTokenProvider, "getAuthVersion", token)).isEqualTo(3);
        assertThat((String) invoke(jwtTokenProvider, "getTokenType", token)).isEqualTo("access");
        log.info("Access Token: {}", token);
        log.info("Parsed User ID: {}, Auth Version: {}, Token Type: {}",
				invoke(jwtTokenProvider, "getEmpId", token),
				invoke(jwtTokenProvider, "getAuthVersion", token),
				invoke(jwtTokenProvider, "getTokenType", token));
    }

    @Test
    void refreshTokenIsRejectedByAccessTokenValidation() throws Exception {
        Object jwtTokenProvider = newJwtTokenProvider();
        String refreshToken = invoke(jwtTokenProvider, "createRefreshToken", 10L, 3, "session-1");

        assertThat((String) invoke(jwtTokenProvider, "getTokenType", refreshToken)).isEqualTo("refresh");
        assertThat((String) invoke(jwtTokenProvider, "getSessionId", refreshToken)).isEqualTo("session-1");
        assertThat((Boolean) invoke(jwtTokenProvider, "validateRefreshToken", refreshToken)).isTrue();
        assertThatThrownBy(() -> invoke(jwtTokenProvider, "validateAccessToken", refreshToken))
                .isInstanceOf(InvocationTargetException.class)
                .satisfies(error -> assertErrorCode(error.getCause(), "INVALID_TOKEN"));
        
        log.info("Refresh Token: {}", refreshToken);
    }

    private Object newJwtTokenProvider() throws Exception {
        Class<?> type = Class.forName("com.mycrewsoft.security.jwt.JwtTokenProvider");
        return type.getConstructor(String.class, long.class, long.class)
                .newInstance("12345678901234567890123456789012", 60000L, 120000L);
    }

    private void assertErrorCode(Throwable error, String expectedErrorCode) {
        try {
            assertThat(error.getClass().getName()).isEqualTo("com.mycrewsoft.common.exception.CustomException");
            assertThat(invoke(error, "getErrorCode").toString()).isEqualTo(expectedErrorCode);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(Object target, String methodName, Object... args) throws Exception {
        Class<?>[] parameterTypes = new Class<?>[args.length];

        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = switch (args[i]) {
                default -> args[i].getClass();
            };
        }

        return (T) target.getClass()
                .getMethod(methodName, parameterTypes)
                .invoke(target, args);
    }
}
