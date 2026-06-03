package com.mycrewsoft.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.mycrewsoft.config.websocket.StompChannelInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * WebSocket + STOMP 설정 클래스.

 * [목적지 구조]
 * /pub/... → 클라이언트가 서버로 메시지를 보낼 때 (publish)
 * /sub/... → 클라이언트가 메시지를 받기 위해 구독할 때 (subscribe)
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed-origins}")
    private String allowedOrigins;
    
    private final StompChannelInterceptor stompChannelInterceptor;
    /**
     * 메시지 브로커를 설정한다.
     * 브로커란 메시지를 중간에서 받아서 구독자들에게 전달해주는 우체국 같은 역할이다.
     *
     * enableSimpleBroker("/sub")
     *   → /sub으로 시작하는 목적지로 오는 메시지는 브로커가 해당 목적지를 구독 중인
     *     모든 클라이언트에게 자동으로 전달한다.
     *   → MsngrServiceImpl에서 /sub/chat/{chtrmId}로 브로드캐스트하는 것과 연결된다.
     *
     * setApplicationDestinationPrefixes("/pub")
     *   → /pub으로 시작하는 목적지로 오는 메시지는 서버의 @MessageMapping 메서드로 라우팅한다.
     *   → 클라이언트가 /pub/chat.send/1로 보내면
     *     MsngrWebSocketController의 @MessageMapping("/chat.send/{chtrmId}")가 실행된다.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 클라이언트가 WebSocket 연결을 맺을 엔드포인트를 등록한다.
     *
     * setAllowedOriginPatterns("*")
     *   → 모든 출처(도메인)에서 연결을 허용한다.
     *   → 개발 중에는 * 로 열어두고, 운영 환경에서는 실제 프론트 도메인으로 좁혀야 한다.
     *
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*");
    }

    /**
     * 클라이언트에서 서버로 오는 인바운드 채널에 인터셉터를 등록한다.
     * 이 설정으로 STOMP CONNECT 시점에 StompChannelInterceptor가 JWT를 검증한다.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompChannelInterceptor);
    }

}