package com.mycrewsoft.domain.mail.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.mail.mapper.MailMapper;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailAccountTokenService {

    private static final String TOKEN_STATUS_ACTIVE = "ACTIVE";
    private static final String TOKEN_STATUS_INVALID = "INVALID";

    private final MailMapper mailMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markInvalid(MailAccountVO account) {
        mailMapper.updateMailAccountTokens(
                account.getEmpId(),
                account.getAccessToken(),
                account.getTokenExprDt(),
                TOKEN_STATUS_INVALID);
        account.setTokenStatusCd(TOKEN_STATUS_INVALID);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateActive(MailAccountVO account, String accessToken, LocalDateTime expiresAt) {
        mailMapper.updateMailAccountTokens(
                account.getEmpId(),
                accessToken,
                expiresAt,
                TOKEN_STATUS_ACTIVE);
        account.setAccessToken(accessToken);
        account.setTokenExprDt(expiresAt);
        account.setTokenStatusCd(TOKEN_STATUS_ACTIVE);
    }
}
