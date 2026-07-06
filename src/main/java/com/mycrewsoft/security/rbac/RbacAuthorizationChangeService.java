package com.mycrewsoft.security.rbac;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 역할/권한 변경 시 사용자의 권한 버전을 증가시키는 서비스.
 *
 * 역할:
 * - 특정 사용자의 authVersion을 증가시킨다.
 * - 특정 역할이 변경되었을 때 해당 역할을 가진 사용자들의 authVersion을 증가시킨다.
 *
 * 설계 기준:
 * - 세션을 강제로 삭제하지 않는다.
 * - 다음 요청에서 RbacSessionRefreshService가 버전 차이를 감지해 Redis 세션을 최신화한다.
 */
@Service
@RequiredArgsConstructor
public class RbacAuthorizationChangeService {

    private final RbacMapper rbacMapper;

    @Transactional
    public void refreshEmployeePermissions(Long empId) {
        if (empId == null) {
            return;
        }

        rbacMapper.incrementAuthVersionForEmpId(empId);
    }

    @Transactional
    public void refreshEmployeesPermissions(List<Long> empIds) {
        if (empIds == null) {
            return;
        }

        empIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(this::refreshEmployeePermissions);
    }

    @Transactional
    public void refreshRolePermissions(Long roleId) {
        if (roleId == null) {
            return;
        }

        List<Long> empIds = rbacMapper.selectEnabledEmpIdsByRoleId(roleId);
        if (empIds == null) {
            return;
        }

        empIds.stream()
                .distinct()
                .forEach(this::refreshEmployeePermissions);
    }

    @Deprecated
    public void invalidateEmployeePermissions(Long empId) {
        refreshEmployeePermissions(empId);
    }

    @Deprecated
    public void invalidateRolePermissions(Long roleId) {
        refreshRolePermissions(roleId);
    }
}
