package com.mycrewsoft.domain.mail.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mycrewsoft.domain.mail.mapper.MailMapper;
import com.mycrewsoft.domain.mail.service.MailService;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 활성 메일 계정을 주기적으로 폴링하여 Gmail 신규/변경 메일을 동기화한다.
 * (실시간 Pub/Sub 푸시 대신 폴링 방식. 사용자 진입 시 동기화와 별개로 백그라운드 최신화)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailSyncScheduler {

    private static final int POLLING_MAX_RESULTS = 30;

    private final MailMapper mailMapper;
    private final MailService mailService;

    @Scheduled(
            fixedDelayString = "${mail.sync.polling-interval-ms:300000}",
            initialDelayString = "${mail.sync.polling-initial-delay-ms:60000}")
    public void pollActiveAccounts() {
        List<MailAccountVO> accounts = mailMapper.selectActiveMailAccounts();
        if (accounts == null || accounts.isEmpty()) {
            return;
        }
        int ok = 0;
        int fail = 0;
        for (MailAccountVO account : accounts) {
            try {
                mailService.syncAccount(account, POLLING_MAX_RESULTS);
                ok++;
            } catch (Exception e) {
                fail++;
                log.warn("Scheduled mail sync failed. empId={}", account.getEmpId(), e);
            }
        }
        log.info("Scheduled mail sync done. total={}, ok={}, fail={}", accounts.size(), ok, fail);
    }
}
