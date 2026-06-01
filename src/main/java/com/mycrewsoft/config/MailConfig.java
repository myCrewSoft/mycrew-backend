package com.mycrewsoft.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.mycrewsoft.domain.mail.config.GoogleOAuthProperties;

@Configuration
@EnableConfigurationProperties(GoogleOAuthProperties.class)
public class MailConfig {
}
