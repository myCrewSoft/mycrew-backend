package com.mycrewsoft.config.websocket;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * WebSocket STOMP 메시지에 대한 인터셉터.
 * JWT토큰을 사용하여 인증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 메시지가 채널로 전송되기 직전에 실행된다.
     * CONNECT 명령일 때만 JWT를 검증하고, 나머지 명령(SEND, SUBSCRIBE 등)은 그냥 통과시킨다.
     *
     * [StompHeaderAccessor란?]
     * STOMP 메시지의 헤더(목적지, 명령어, 인증 토큰 등)에 접근할 수 있게 해주는 래퍼 클래스다.
     *
     * @param message 클라이언트가 보낸 STOMP 메시지
     * @param channel 메시지가 전달될 채널
     * @return 검증 통과 시 원본 메시지 반환, 실패 시 예외 발생
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new CustomException(ErrorCode.WS_UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                jwtTokenProvider.validateToken(token);
            } catch (CustomException e) {
                throw new CustomException(ErrorCode.WS_INVALID_TOKEN);
            }

            Long empId = jwtTokenProvider.getEmpId(token);

            // empId를 Principal로 등록해서 이후 SEND 명령에서도 꺼낼 수 있게 함
            accessor.setUser(new java.security.Principal() {
                @Override
                public String getName() {
                    return String.valueOf(empId);
                }
            });

            log.info("WebSocket CONNECT 인증 성공 - empId: {}", empId);

        }

        return message;
    }
}