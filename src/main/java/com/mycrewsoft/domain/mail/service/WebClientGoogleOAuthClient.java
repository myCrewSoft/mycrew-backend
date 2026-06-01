package com.mycrewsoft.domain.mail.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.mail.config.GoogleOAuthProperties;
import com.mycrewsoft.domain.mail.dto.GoogleTokenResponse;
import com.mycrewsoft.domain.mail.dto.GoogleUserInfoResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebClientGoogleOAuthClient implements GoogleOAuthClient {

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final WebClient.Builder webClientBuilder;
    private final GoogleOAuthProperties properties;

    @Override
    public GoogleTokenResponse exchangeCode(String code) {
        GoogleTokenResponse response = webClientBuilder.build()
                .post()
                .uri(TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("code", code)
                        .with("client_id", properties.getClientId())
                        .with("client_secret", properties.getClientSecret())
                        .with("redirect_uri", properties.getRedirectUri())
                        .with("grant_type", "authorization_code"))
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .block();

        if (response == null || response.getAccessToken() == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        return response;
    }

    @Override
    public GoogleUserInfoResponse fetchUserInfo(String accessToken) {
        GoogleUserInfoResponse response = webClientBuilder.build()
                .get()
                .uri(USERINFO_URI)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GoogleUserInfoResponse.class)
                .block();

        if (response == null || response.getEmail() == null || response.getSub() == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        return response;
    }
}
