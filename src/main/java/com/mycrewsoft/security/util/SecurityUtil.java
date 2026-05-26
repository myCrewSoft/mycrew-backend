package com.mycrewsoft.security.util;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.authz.ScopedPermission;
import com.mycrewsoft.security.users.AuthorizationUserDetails;

/**
 * 현재 SecurityContext의 인증 사용자 정보를 조회하는 유틸리티.
 *
 * 역할:
 * - 현재 로그인한 사용자 ID를 가져온다.
 * - 현재 사용자의 scopedPermissions를 가져온다.
 * - 인증되지 않았거나 예상 타입이 아닌 경우 공통 예외를 발생시킨다.
 *
 * 사용 위치:
 * - 도메인 서비스에서 현재 사용자 기준 인가 판단이 필요할 때 사용한다.
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static AuthorizationUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AuthorizationUserDetails userDetails)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return userDetails;
    }

    public static Long getCurrentEmpId() {
        return getCurrentUser().getEmpId();
    }

    public static List<ScopedPermission> getCurrentScopedPermissions() {
        List<ScopedPermission> scopedPermissions = getCurrentUser().getScopedPermissions();
        return scopedPermissions == null ? List.of() : scopedPermissions;
    }
}
