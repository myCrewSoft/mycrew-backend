package com.mycrewsoft.domain.mail.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.StringJoiner;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.mail.config.GoogleOAuthProperties;
import com.mycrewsoft.domain.mail.dto.response.GoogleOAuthAuthorizeResponse;
import com.mycrewsoft.domain.mail.dto.response.GoogleTokenResponse;
import com.mycrewsoft.domain.mail.dto.response.GoogleUserInfoResponse;
import com.mycrewsoft.domain.mail.mapper.MailAccountMapper;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private static final String GOOGLE_PROVIDER = "GOOGLE";
    private static final String AUTHORIZATION_URI = "https://accounts.google.com/o/oauth2/v2/auth";

    private final GoogleOAuthProperties properties;
    private final GoogleOAuthStateStore stateStore;
    private final GoogleOAuthClient googleOAuthClient;
    private final MailAccountMapper mailAccountMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public GoogleOAuthAuthorizeResponse createAuthorizationUrl(Long empId) {
        return createAuthorizationUrl(empId, null);
    }

    public GoogleOAuthAuthorizeResponse createAuthorizationUrl(Long empId, String context) {
        if (empId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        String state = createState();
        stateStore.save(state, empId, normalizeContext(context));

        String authorizationUrl = UriComponentsBuilder.fromUriString(AUTHORIZATION_URI)
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", properties.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", scopeText())
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build()
                .toUriString();

        return new GoogleOAuthAuthorizeResponse(authorizationUrl);
    }

    @Transactional
    public String handleCallback(String code, String state) {
        if (!StringUtils.hasText(code) || !StringUtils.hasText(state)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        GoogleOAuthState oauthState = stateStore.consume(state);
        Long empId = oauthState.empId();
        GoogleTokenResponse token = googleOAuthClient.exchangeCode(code);
        GoogleUserInfoResponse userInfo = googleOAuthClient.fetchUserInfo(token.getAccessToken());

        MailAccountVO mailAccount = new MailAccountVO();
        mailAccount.setEmpId(empId);
        mailAccount.setProviderCd(GOOGLE_PROVIDER);
        mailAccount.setEmailAddr(userInfo.getEmail());
        mailAccount.setGoogleSubId(userInfo.getSub());
        mailAccount.setAccessToken(token.getAccessToken());
        mailAccount.setRefreshToken(token.getRefreshToken());
        mailAccount.setTokenExprDt(expiresAt(token.getExpiresIn()));
        mailAccount.setScopeCn(StringUtils.hasText(token.getScope()) ? token.getScope() : scopeText());
        mailAccount.setTokenStatusCd("ACTIVE");
        mailAccount.setUseYn("Y");
        mailAccount.setConnectedDt(LocalDateTime.now());
        mailAccount.setFrstRegDt(LocalDateTime.now());

        mailAccountMapper.upsertGoogleMailAccount(mailAccount);
        return successUri(oauthState.context());
    }

    private String createState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String scopeText() {
        StringJoiner joiner = new StringJoiner(" ");
        properties.getScopes().forEach(joiner::add);
        return joiner.toString();
    }

    private String normalizeContext(String context) {
        if (!StringUtils.hasText(context)) {
            return null;
        }
        return "mail".equalsIgnoreCase(context.trim()) ? "mail" : null;
    }

    private String successUri(String context) {
        if ("mail".equals(context) && StringUtils.hasText(properties.getFrontendMailSuccessUri())) {
            return properties.getFrontendMailSuccessUri();
        }
        return properties.getFrontendSuccessUri();
    }

    private LocalDateTime expiresAt(Long expiresIn) {
        if (expiresIn == null) {
            return null;
        }
        return LocalDateTime.now().plusSeconds(expiresIn);
    }
}
