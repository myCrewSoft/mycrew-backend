package com.mycrewsoft.security.authz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScopedPermission {

    private String permCd;
    private Long roleId;
    private String roleName;
    private ScopeType scopeType;
    private String scopeId;

    public static ScopedPermission of(
            String permCd,
            Long roleId,
            String roleName,
            ScopeType scopeType,
            String scopeId) {
        return ScopedPermission.builder()
                .permCd(permCd)
                .roleId(roleId)
                .roleName(roleName)
                .scopeType(scopeType)
                .scopeId(scopeId)
                .build();
    }
}
