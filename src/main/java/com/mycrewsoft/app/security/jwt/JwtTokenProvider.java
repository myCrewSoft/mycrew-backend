package com.mycrewsoft.app.security.jwt;

import com.mycrewsoft.app.common.exception.CustomException;
import com.mycrewsoft.app.common.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Access Token 과 Refresh Token 의 생성, 파싱, 검증을 담당하는 컴포넌트.
 * application.properties 의 jwt.* 설정값을 주입받아 사용한다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

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

    /**
     * Access Token 을 생성한다.
     * roles 클레임에 권한 목록을 담는다. roles 누락 시 @PreAuthorize 동작 안 함.
     */
    public String createAccessToken(Authentication authentication) {
        try {
            JWSSigner signer = new MACSigner(sharedSecret);

            // Authentication 에서 직접 뽑아씀
            String username = authentication.getName();
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("roles", roles)
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

    /** Refresh Token 을 생성한다. roles 클레임을 포함하지 않는다. */
    public String createRefreshToken(String username) {
        try {
            JWSSigner signer = new MACSigner(sharedSecret);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(username)
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

    /** 토큰에서 username 을 추출한다. */
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰을 파싱하여 Spring Security 의 Authentication 객체를 생성한다.
     * roles 클레임에서 권한 정보를 꺼내 GrantedAuthority 목록을 구성한다.
     * SecurityContextHolder 에 저장할 인증 객체로 사용한다.
     *
     * @param token 파싱할 JWT 토큰 문자열
     * @return 인증 정보가 담긴 Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        JWTClaimsSet claims = parseClaims(token);

        List<SimpleGrantedAuthority> authorities = ((List<?>) claims.getClaim("roles")).stream()
                .map(role -> new SimpleGrantedAuthority(role.toString()))
                .collect(Collectors.toList());

        UserDetails userDetails = new User(claims.getSubject(), "", authorities);
        
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
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