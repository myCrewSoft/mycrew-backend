package com.mycrewsoft.security.users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mycrewsoft.security.authz.ScopedPermission;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Spring Security 인증에 사용하는 사용자 Principal.
 *
 * 역할:
 * - 인증된 사원의 EMP_ID, 로그인명, 권한 목록, 세션 ID, 권한 버전 정보를 담는다.
 * - AuthorizationService가 사용할 scopedPermissions를 제공한다.
 *
 * 포함 권한:
 * - authorities: Spring Security의 전역 권한 정보
 * - scopedPermissions: 리소스 범위 기반 인가 판단에 사용하는 동적 권한 정보
 */
@Getter
@Setter
@NoArgsConstructor
public class AuthorizationUserDetails implements UserDetails {

    private Long empId;
    private String username;
    private String password;
    private boolean enabled;
    private String empStat;
    private Integer authVersion;
    private Collection<? extends GrantedAuthority> authorities;
    private List<ScopedPermission> scopedPermissions = new ArrayList<>();
    private String sessionId;

    public AuthorizationUserDetails(
            Long empId,
            String username,
            String password,
            boolean enabled,
            Integer authVersion,
            Collection<? extends GrantedAuthority> authorities,
            List<ScopedPermission> scopedPermissions) {
        this(empId, username, password, enabled, null, authVersion, authorities, scopedPermissions, null);
    }

    public AuthorizationUserDetails(
            Long empId,
            String username,
            String password,
            boolean enabled,
            String empStat,
            Integer authVersion,
            Collection<? extends GrantedAuthority> authorities,
            List<ScopedPermission> scopedPermissions) {
        this(empId, username, password, enabled, empStat, authVersion, authorities, scopedPermissions, null);
    }

    public AuthorizationUserDetails(
            Long empId,
            String username,
            String password,
            boolean enabled,
            String empStat,
            Integer authVersion,
            Collection<? extends GrantedAuthority> authorities,
            List<ScopedPermission> scopedPermissions,
            String sessionId) {
        this.empId = empId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.empStat = empStat;
        this.authVersion = authVersion;
        this.authorities = authorities;
        this.scopedPermissions = scopedPermissions == null ? new ArrayList<>() : scopedPermissions;
        this.sessionId = sessionId;
    }

    public AuthorizationUserDetails(
            Long empId,
            String username,
            String password,
            boolean enabled,
            String empStat,
            Integer authVersion,
            Collection<? extends GrantedAuthority> authorities) {
        this.empId = empId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
	    this.empStat = empStat;
        this.authVersion = authVersion;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username == null ? String.valueOf(empId) : username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
