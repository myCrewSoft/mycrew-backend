package com.mycrewsoft.domain.mail.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleOAuthAuthorizeResponse {

    private final String authorizationUrl;
}
