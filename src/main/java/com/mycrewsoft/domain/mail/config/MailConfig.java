package com.mycrewsoft.domain.mail.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GoogleOAuthProperties.class)
public class MailConfig {
}
