package com.mycrewsoft.security.users;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mycrewsoft.security.authz.ScopedPermission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthSession {

    private String sessionId;
    private Long userId;
    private String username;
    private boolean enabled;
    private Integer authVersion;
    private Set<String> authorities = new LinkedHashSet<>();
    private List<ScopedPermission> scopedPermissions = new ArrayList<>();
    private String refreshTokenHash;

    public AuthSession(
            String sessionId,
            Long userId,
            String username,
            boolean enabled,
            Integer authVersion,
            Set<String> authorities,
            String refreshTokenHash) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.username = username;
        this.enabled = enabled;
        this.authVersion = authVersion;
        this.authorities = authorities;
        this.refreshTokenHash = refreshTokenHash;
    }
}
