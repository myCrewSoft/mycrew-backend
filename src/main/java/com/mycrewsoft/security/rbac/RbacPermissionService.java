package com.mycrewsoft.security.rbac;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mycrewsoft.security.authz.ScopedPermission;

import lombok.RequiredArgsConstructor;

/**
 * 사용자에게 부여된 scoped permission을 조회하는 서비스.
 *
 * 역할:
 * - RbacMapper를 통해 DB의 RBAC 정보를 읽는다.
 * - null 조회 결과를 빈 목록으로 정리해 상위 인증/인가 로직이 안전하게 사용할 수 있게 한다.
 *
 * 사용 위치:
 * - 로그인 세션 생성 시 권한 적재
 * - 권한 버전 변경 감지 후 세션 권한 재적재
 */
@Service
@RequiredArgsConstructor
public class RbacPermissionService {

    private final RbacMapper rbacMapper;

    public List<ScopedPermission> loadScopedPermissions(Long empId) {
        List<ScopedPermission> permissions = rbacMapper.selectScopedPermissionsByEmpId(empId);
        return permissions == null ? List.of() : List.copyOf(permissions);
    }
}
