package com.mycrewsoft.security.rbac;

import com.mycrewsoft.security.users.AuthSession;

/**
 * 인증 세션 저장소가 제공해야 하는 갱신 기능을 정의하는 인터페이스.
 *
 * 역할:
 * - RBAC 계층이 구체적인 Redis 구현에 직접 의존하지 않도록 분리한다.
 * - 권한 버전 변경 감지 후 갱신된 AuthSession을 저장소에 반영한다.
 *
 * 구현체:
 * - RefreshTokenService
 */
public interface AuthSessionStore {

    void updateSession(AuthSession session);
}
