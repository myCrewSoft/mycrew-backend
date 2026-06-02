package com.mycrewsoft.security.rbac;

import com.mycrewsoft.common.constant.PermissionCode;

/**
 * PermissionCode enum을 TB_PERMISSION 마스터 데이터로 동기화할 때 사용하는 값 객체.
 *
 * 역할:
 * - enum 권한 코드를 DB insert용 데이터로 변환한다.
 * - TB_PERMISSION.PERM_CD, PERM_NM, PERM_EXPLN에 들어갈 값을 한 번에 전달한다.
 */
public record PermissionSeed(
        String code,
        String name,
        String description) {

    public static PermissionSeed from(PermissionCode permissionCode) {
        String code = permissionCode.getCode();
        String name = permissionCode.getPermissionName();
        String description = permissionCode.getDescription();
        
        return new PermissionSeed(
                code,
                name,
                description);
    }
}
