package com.mycrewsoft.domain.mail.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "google.oauth")
public class GoogleOAuthProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String frontendSuccessUri;
    private String frontendFailureUri;
    private String frontendMailSuccessUri;
    private String frontendMyPageSuccessUri;
    private String frontendMyPageFailureUri;
    private List<String> scopes = new ArrayList<>();
}
