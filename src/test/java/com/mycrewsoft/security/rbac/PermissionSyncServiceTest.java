package com.mycrewsoft.security.rbac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycrewsoft.common.constant.PermissionCode;

@ExtendWith(MockitoExtension.class)
class PermissionSyncServiceTest {

    @Mock
    private PermissionSyncMapper permissionSyncMapper;

    @Test
    void syncPermissionCodesMergesPermissionsThenMapsSuperAdminPermissions() {
        PermissionSyncService permissionSyncService = new PermissionSyncService(permissionSyncMapper);

        when(permissionSyncMapper.mergePermissions(argThat(this::containsAllPermissionCodes)))
                .thenReturn(3);

        int insertedCount = permissionSyncService.syncPermissionCodes();

        assertThat(insertedCount).isEqualTo(3);

        InOrder order = inOrder(permissionSyncMapper);
        order.verify(permissionSyncMapper)
                .mergePermissions(argThat(this::containsAllPermissionCodes));
        order.verify(permissionSyncMapper).mergeSuperAdminPermissions();
    }

    private boolean containsAllPermissionCodes(List<PermissionSeed> permissions) {
        if (permissions == null) {
            return false;
        }

        List<String> permissionCodes = permissions.stream()
                .map(PermissionSeed::code)
                .toList();

        return permissionCodes.containsAll(
                java.util.Arrays.stream(PermissionCode.values())
                        .map(PermissionCode::getCode)
                        .toList());
    }
}
