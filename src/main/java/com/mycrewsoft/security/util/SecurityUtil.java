package com.mycrewsoft.security.util;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.authz.ScopedPermission;
import com.mycrewsoft.security.users.AuthorizationUserDetails;

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
        return getCurrentUser().getMbrId();
    }

    public static List<ScopedPermission> getCurrentScopedPermissions() {
        List<ScopedPermission> scopedPermissions = getCurrentUser().getScopedPermissions();
        return scopedPermissions == null ? List.of() : scopedPermissions;
    }
}
