package com.mycrewsoft.security.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mycrewsoft.security.authz.ScopeType;
import com.mycrewsoft.security.authz.ScopedPermission;
import com.mycrewsoft.security.rbac.RbacMapper;
import com.mycrewsoft.security.rbac.RbacPermissionService;
import com.mycrewsoft.security.users.AuthorizationUserDetails;
import com.mycrewsoft.security.users.AuthorizationUserMapper;
import com.mycrewsoft.security.users.AuthorizationUserRecord;

class AuthorizationUserDetailsServiceTest {

    @Test
    void loadUserByUsernameReturnsPrincipalWithAuthoritiesAndScopedPermissions() {
        FakeAuthorizationUserMapper userMapper = new FakeAuthorizationUserMapper();
        FakeRbacMapper rbacMapper = new FakeRbacMapper();
        rbacMapper.authVersion = 7;
        rbacMapper.authorities = List.of("ROLE_USER");
        rbacMapper.permissions = List.of(
                ScopedPermission.of("PROJECT_MANAGE", 20L, "project manager", ScopeType.PROJECT, "300"));
        AuthorizationUserDetailsService service = new AuthorizationUserDetailsService(
                userMapper,
                rbacMapper,
                new RbacPermissionService(rbacMapper));

        AuthorizationUserDetails details =
                (AuthorizationUserDetails) service.loadUserByUsername("login-100");

        assertThat(userMapper.requestedUsername).isEqualTo("login-100");
        assertThat(details.getEmpId()).isEqualTo(100L);
        assertThat(details.getPassword()).isEqualTo("{noop}secret");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthVersion()).isEqualTo(7);
        assertThat(details.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_USER");
        assertThat(details.getScopedPermissions())
                .extracting(ScopedPermission::getPermCd, ScopedPermission::getScopeType, ScopedPermission::getScopeId)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("PROJECT_MANAGE", ScopeType.PROJECT, "300"));
    }

    private static class FakeAuthorizationUserMapper implements AuthorizationUserMapper {

        private String requestedUsername;

        @Override
        public AuthorizationUserRecord selectLoginUserByUsername(String username) {
            this.requestedUsername = username;
            return new AuthorizationUserRecord(100L, "login-100", "{noop}secret", true);
        }
    }

    private static class FakeRbacMapper implements RbacMapper {

        private Integer authVersion = 0;
        private List<String> authorities = List.of();
        private List<ScopedPermission> permissions = List.of();

        @Override
        public List<ScopedPermission> selectScopedPermissionsByEmpId(Long empId) {
            return permissions;
        }

        @Override
        public int incrementAuthVersionForEmpId(Long empId) {
            return 0;
        }

        @Override
        public List<Long> selectEnabledEmpIdsByRoleId(Long roleId) {
            return List.of();
        }

        @Override
        public Integer selectAuthVersionByEmpId(Long empId) {
            return authVersion;
        }

        @Override
        public List<String> selectAuthoritiesByEmpId(Long empId) {
            return authorities;
        }
    }
}
