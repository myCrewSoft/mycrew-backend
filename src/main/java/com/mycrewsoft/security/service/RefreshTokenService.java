// package com.mycrewsoft.security.service;

// import lombok.RequiredArgsConstructor;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.stereotype.Service;
// import java.util.concurrent.TimeUnit;

// /**
//  * Redis 를 사용하여 Refresh Token 을 관리하는 서비스.
//  * 저장, 유효성 검증, 삭제(로그아웃) 기능을 제공한다.
//  * Key 구조: "refresh:{username}" → Refresh Token 값
//  */
// @Service
// @RequiredArgsConstructor
// public class RefreshTokenService {

//     private final RedisTemplate<String, String> redisTemplate;
//     private static final String REFRESH_PREFIX = "refresh:";

//     /**
//      * Refresh Token 을 Redis 에 저장한다.
//      * Key 는 "refresh:{username}" 형식이며, TTL 이 지나면 자동으로 삭제된다.
//      *
//      * @param username     사용자명 (Redis 키에 포함됨)
//      * @param refreshToken 저장할 Refresh Token 문자열
//      * @param expirationMs 만료 시간 (밀리초). application.properties 의
//      *                     jwt.refresh-token-expiration 값
//      */
//     public void save(String username, String refreshToken, long expirationMs) {
//         redisTemplate.opsForValue().set(
//                 REFRESH_PREFIX + username, refreshToken, expirationMs, TimeUnit.MILLISECONDS);
//     }

//     /**
//      * Redis 에 저장된 Refresh Token 이 전달받은 토큰과 일치하는지 검증한다.
//      * Redis 에 토큰이 없거나 값이 다르면 false 를 반환한다.
//      *
//      * @param username     사용자명
//      * @param refreshToken 검증할 Refresh Token 문자열
//      * @return 저장된 토큰과 일치하면 true, 아니면 false
//      */
//     public boolean isValid(String username, String refreshToken) {
//         String stored = redisTemplate.opsForValue().get(REFRESH_PREFIX + username);

//         return refreshToken.equals(stored);
//     }

//     /** 로그아웃 시 Redis 에서 Refresh Token 을 삭제한다. */
//     public void delete(String username) {
//         redisTemplate.delete(REFRESH_PREFIX + username);
//     }
// }