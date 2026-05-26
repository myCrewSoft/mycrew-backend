package com.mycrewsoft.security.rbac;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 애플리케이션 시작 시 PermissionCode enum을 TB_PERMISSION에 동기화하는 Runner.
 *
 * 활성화 방법:
 * - security.rbac.permission-sync.enabled=true
 *
 * 설계 기준:
 * - 기본값은 비활성화하여 운영 DB에 예기치 않은 쓰기가 발생하지 않게 한다.
 * - 활성화되면 없는 권한 코드만 추가하므로 반복 실행해도 중복 insert 되지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "security.rbac.permission-sync",
        name = "enabled",
        havingValue = "true")
public class PermissionSyncRunner implements ApplicationRunner {

    private final PermissionSyncService permissionSyncService;

    @Override
    public void run(ApplicationArguments args) {
        int insertedCount = permissionSyncService.syncPermissionCodes();
        log.info("PermissionCode sync completed. insertedCount={}", insertedCount);
    }
}
