package com.mycrewsoft.security.authz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자에게 부여된 하나의 범위 기반 권한을 표현하는 모델.
 *
 * 역할:
 * - 권한 코드, 역할, scopeType, scopeId를 함께 담는다.
 * - AuthorizationService가 리소스 접근 가능 여부를 판단할 때 사용한다.
 *
 * 예:
 * - BOARD_POST_UPDATE + DEPT + 10
 * - PROJECT_DRIVE_READ + PROJECT + 3001
 * - EMPLOYEE_UPDATE + SELF + SELF
 */
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
