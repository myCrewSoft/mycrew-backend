package com.mycrewsoft.security.rbac;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.security.authz.PermissionCode;

import lombok.RequiredArgsConstructor;

/**
 * PermissionCode enum을 기준으로 TB_PERMISSION 마스터 데이터를 동기화하는 서비스.
 *
 * 역할:
 * - PermissionCode.values()를 읽어 DB 저장용 PermissionSeed 목록을 만든다.
 * - TB_PERMISSION에 아직 없는 권한 코드만 추가한다.
 *
 * 주의:
 * - 역할과 권한의 연결(TB_PERMISSION_ROLE_MAPPING)은 자동으로 만들지 않는다.
 * - 어떤 역할에 어떤 권한을 줄지는 관리자/시드 데이터에서 명시적으로 결정해야 한다.
 */
@Service
@RequiredArgsConstructor
public class PermissionSyncService {

    private final PermissionSyncMapper permissionSyncMapper;

    @Transactional
    public int syncPermissionCodes() {
        List<PermissionSeed> permissions = Arrays.stream(PermissionCode.values())
                .map(PermissionSeed::from)
                .toList();

        if (permissions.isEmpty()) {
            return 0;
        }

        return permissionSyncMapper.mergePermissions(permissions);
    }
}
