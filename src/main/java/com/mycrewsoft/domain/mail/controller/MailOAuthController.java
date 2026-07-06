package com.mycrewsoft.domain.mail.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.mail.config.GoogleOAuthProperties;
import com.mycrewsoft.domain.mail.dto.response.GoogleOAuthAuthorizeResponse;
import com.mycrewsoft.domain.mail.service.GoogleOAuthService;
import com.mycrewsoft.security.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Mail OAuth", description = "메일 계정 OAuth 연동 API")
public class MailOAuthController {

    private final GoogleOAuthService googleOAuthService;
    private final GoogleOAuthProperties googleOAuthProperties;

    @GetMapping("/mail/oauth/google/authorize")
    @Operation(
            summary = "Google OAuth 인증 URL 발급",
            description = "현재 로그인한 사원이 Gmail 계정을 연동할 수 있도록 Google OAuth 인증 URL을 생성."
        )
    public ResponseEntity<ApiResponse<GoogleOAuthAuthorizeResponse>> authorize(
            @RequestParam(required = false) String context) {
        GoogleOAuthAuthorizeResponse response =
                googleOAuthService.createAuthorizationUrl(SecurityUtil.getCurrentEmpId(), context);

        return ResponseEntity.ok(ApiResponse.success("Google 메일 연결 URL 생성 완료", response));
    }

    @GetMapping("/mail/oauth/google/callback")
    @Operation(
            summary = "Google OAuth 콜백 처리",
            description = "Google OAuth 인증 완료 후 전달된 authorization code를 처리하여 Gmail 계정을 사원 계정에 연동."
        )
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
