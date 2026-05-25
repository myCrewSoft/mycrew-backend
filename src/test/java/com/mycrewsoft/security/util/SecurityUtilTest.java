package com.mycrewsoft.security.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.authz.ScopeType;
import com.mycrewsoft.security.authz.ScopedPermission;
import com.mycrewsoft.security.users.AuthorizationUserDetails;

class SecurityUtilTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentEmpIdReturnsAuthenticatedPrincipalId() {
        setAuthenticatedUser();

        assertThat(SecurityUtil.getCurrentEmpId()).isEqualTo(100L);
    }

    @Test
    void getCurrentScopedPermissionsReturnsAuthenticatedPrincipalPermissions() {
        setAuthenticatedUser();

        assertThat(SecurityUtil.getCurrentScopedPermissions())
                .extracting("permCd", "scopeType", "scopeId")
                .containsExactly(org.assertj.core.api.Assertions.tuple("BOARD:DELETE", ScopeType.DEPT, "10"));
    }

    @Test
    void getCurrentUserThrowsUnauthorizedWhenAuthenticationIsMissing() {
        assertThatThrownBy(SecurityUtil::getCurrentUser)
                .isInstanceOf(CustomException.class)
                .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
                        .isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    private void setAuthenticatedUser() {
        AuthorizationUserDetails principal = new AuthorizationUserDetails(
                100L,
                "user-100",
                null,
                true,
                1,
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                List.of(ScopedPermission.of("BOARD:DELETE", 10L, "board manager", ScopeType.DEPT, "10")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
