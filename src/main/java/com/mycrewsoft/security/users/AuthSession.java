package com.mycrewsoft.security.users;

import java.util.LinkedHashSet;
import java.util.Set;

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
    private String refreshTokenHash;
}
