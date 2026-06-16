package com.mycrewsoft.domain.video.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "livekit")
public class LiveKitProperties {

    // LiveKit 서버 웹소켓 주소
    private String url;

    // LiveKit API 키
    private String apiKey;

    // LiveKit API 시크릿
    private String apiSecret;
}