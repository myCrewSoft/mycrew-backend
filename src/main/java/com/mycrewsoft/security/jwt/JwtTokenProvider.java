package com.mycrewsoft.security.jwt;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Access Token 과 Refresh Token 의 생성, 파싱, 검증을 담당하는 컴포넌트.
 * application.properties 의 jwt.* 설정값을 주입받아 사용한다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private static final String AUTH_VERSION_CLAIM = "authVersion";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String SESSION_ID_CLAIM = "sessionId";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final byte[] sharedSecret;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.sharedSecret = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /** Access Token 을 생성한다. WT 에는 사용자 ID 와 권한 버전, Session ID만 담는다. */
    public String createAccessToken(Long userId, Integer authVersion, String sessionId) {
        try {
            JWSSigner signer = new MACSigner(sharedSecret);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(userId))
                    .claim(AUTH_VERSION_CLAIM, authVersion)
                    .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                    .claim(SESSION_ID_CLAIM, sessionId)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + accessTokenExpiration))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /** Refresh Token 을 생성한다. JWT 에는 사용자 ID 와 권한 버전, Session ID만 담는다. */
    public String createRefreshToken(Long userId, Integer authVersion, String sessionId) {
        try {
            JWSSigner signer = new MACSigner(sharedSecret);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(userId))
                    .claim(AUTH_VERSION_CLAIM, authVersion)
                    .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                    .claim(SESSION_ID_CLAIM, sessionId)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    /** 토큰에서 사용자 ID 를 추출한다. */
    public Long getUserId(String token) {
        try {
            return Long.valueOf(parseClaims(token).getSubject());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /** 토큰에서 세션 ID를 추출한다. */
    public String getSessionId(String token) {
        Object sessionId = parseClaims(token).getClaim(SESSION_ID_CLAIM);

        if (sessionId instanceof String value && !value.isBlank()) {
            return value;
        }

        throw new CustomException(ErrorCode.INVALID_TOKEN);
    }
    /** 토큰에서 권한 버전을 추출한다. */
    public Integer getAuthVersion(String token) {
        Object authVersion = parseClaims(token).getClaim(AUTH_VERSION_CLAIM);

        if (authVersion instanceof Number number) {
            return number.intValue();
        }

        throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    /** 토큰 용도(access/refresh)를 추출한다. */
    public String getTokenType(String token) {
        Object tokenType = parseClaims(token).getClaim(TOKEN_TYPE_CLAIM);

        if (tokenType instanceof String value) {
            return value;
        }

        throw new CustomException(ErrorCode.INVALID_TOKEN);
    }
    
    /** 토큰 용도(access)를 검증한다. */
    public boolean validateAccessToken(String token) {
        validateToken(token);

        if (!ACCESS_TOKEN_TYPE.equals(getTokenType(token))) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        return true;
    }
    
    /** 토큰 용도(refresh)를 검증한다. */
    public boolean validateRefreshToken(String token) {
        validateToken(token);

        if (!REFRESH_TOKEN_TYPE.equals(getTokenType(token))) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        return true;
    }

    /**
     * 토큰의 유효성을 검증한다.
     * 만료 → EXPIRED_TOKEN / 그 외 → INVALID_TOKEN
     */
    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(sharedSecret);

            // 서명 검증
            if (!signedJWT.verify(verifier)) {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }

            // 만료 검증
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration.before(new Date())) {
                throw new CustomException(ErrorCode.EXPIRED_TOKEN);
            }

            return true;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰을 파싱하여 Claims 를 반환하는 내부 메서드.
     * 서명 검증까지 수행하므로 변조된 토큰은 JwtException 을 던진다.
     *
     * @param token 파싱할 JWT 토큰 문자열
     * @return 파싱된 Claims 객체
     */
    private JWTClaimsSet parseClaims(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
