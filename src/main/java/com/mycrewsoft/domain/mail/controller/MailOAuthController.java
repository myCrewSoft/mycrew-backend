package com.mycrewsoft.domain.mail.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.mail.config.GoogleOAuthProperties;
import com.mycrewsoft.domain.mail.dto.GoogleOAuthAuthorizeResponse;
import com.mycrewsoft.domain.mail.service.GoogleOAuthService;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MailOAuthController {

    private final GoogleOAuthService googleOAuthService;
    private final GoogleOAuthProperties googleOAuthProperties;

    @GetMapping("/v1/mail/oauth/google/authorize")
    public ResponseEntity<ApiResponse<GoogleOAuthAuthorizeResponse>> authorize() {
        GoogleOAuthAuthorizeResponse response =
                googleOAuthService.createAuthorizationUrl(SecurityUtil.getCurrentEmpId());

        return ResponseEntity.ok(ApiResponse.success("Google 메일 연결 URL 생성 완료", response));
    }

    @GetMapping("/api/mail/oauth/google/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state) {
        try {
            String redirectUri = googleOAuthService.handleCallback(code, state);
            return ResponseEntity.status(302).location(URI.create(redirectUri)).build();
        } catch (RuntimeException e) {
            log.warn("Google mail OAuth callback failed", e);
            return ResponseEntity.status(302)
                    .location(URI.create(googleOAuthProperties.getFrontendFailureUri()))
                    .build();
        }
    }
}
