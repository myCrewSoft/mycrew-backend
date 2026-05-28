package com.mycrewsoft.security.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mycrewsoft.security.rbac.RbacMapper;
import com.mycrewsoft.security.rbac.RbacPermissionService;
import com.mycrewsoft.security.users.AuthorizationUserDetails;
import com.mycrewsoft.security.users.AuthorizationUserMapper;
import com.mycrewsoft.security.users.AuthorizationUserRecord;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security의 UserDetailsService 구현체.
 *
 * 역할:
 * - 로그인 요청 시 사용자 계정을 조회한다.
 * - 사용자의 권한 버전, authorities, scopedPermissions를 함께 조회한다.
 * - AuthorizationUserDetails를 생성해 인증 흐름에 전달한다.
 *
 * 호출 위치:
 * - 로그인 컨트롤러 또는 인증 서비스에서 사용자 인증 정보를 로드할 때 사용한다.
 */
@Service
@RequiredArgsConstructor
public class AuthorizationUserDetailsService implements UserDetailsService {

    private final AuthorizationUserMapper authorizationUserMapper;
    private final RbacMapper rbacMapper;
    private final RbacPermissionService rbacPermissionService;

    public AuthorizationUserDetails loadUserByEmpId(Long empId) {
        return (AuthorizationUserDetails) loadUserByUsername(String.valueOf(empId));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthorizationUserRecord user = authorizationUserMapper.selectLoginUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        Integer authVersion = rbacMapper.selectAuthVersionByEmpId(user.empId());
        List<String> authorityCodes = rbacMapper.selectAuthoritiesByEmpId(user.empId());

        return new AuthorizationUserDetails(
                user.empId(),
                user.username(),
                user.password(),
                Boolean.TRUE.equals(user.enabled()),
                user.empStat(),
                authVersion == null ? 0 : authVersion,
                (authorityCodes == null ? List.<String>of() : authorityCodes).stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList(),
                rbacPermissionService.loadScopedPermissions(user.empId()));
    }
}
