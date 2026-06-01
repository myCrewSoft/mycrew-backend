package com.mycrewsoft.domain.empstat.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 애플리케이션 시작 시 EmpStatCode enum을 TB_EMP_STAT에 동기화하는 Runner.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "domain.emp-stat-sync",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class EmpStatSyncRunner implements ApplicationRunner {

    private final EmpStatSyncService empStatSyncService;

    @Override
    public void run(ApplicationArguments args) {
        int insertedCount = empStatSyncService.syncEmpStatCodes();
        log.info("EmpStatCode sync completed. insertedCount={}", insertedCount);
    }
}
