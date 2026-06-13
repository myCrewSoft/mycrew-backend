package com.mycrewsoft.config.websocket;

import com.mycrewsoft.domain.messenger.service.MsngrService;
import com.mycrewsoft.domain.messenger.enums.ParticipantStatus;
import com.mycrewsoft.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket 세션 연결/끊김 이벤트를 감지하는 리스너.
 *
 * [동작 흐름]
 * 브라우저 연결   → SessionConnectedEvent  → STS1(로그인) 으로 자동 변경
 * 브라우저 종료   → SessionDisconnectEvent → STS4(로그아웃) 으로 자동 변경
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final MsngrService msngrService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * WebSocket 연결이 완료됐을 때 실행된다.
     * STOMP CONNECT 인증을 통과한 직후 시점이다.
     * Authorization 헤더에서 empId를 꺼내 상태를 STS1(로그인)으로 변경한다.
     */
    @EventListener
    public void handleWebSocketConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Long empId = resolveEmpId(accessor);
        if (empId == null) return;

        msngrService.updatePtcptSttusById(empId, ParticipantStatus.ONLINE);
        log.info("WebSocket 연결 - empId: {}, 상태: {}", empId, ParticipantStatus.ONLINE.getCode());
    }

    /**
     * WebSocket 연결이 끊겼을 때 실행된다.
     * 브라우저 종료, 네트워크 단절, 명시적 로그아웃 모두 이 이벤트가 발생한다.
     * 상태를 STS4(로그아웃)으로 자동 변경한다.
     */
    @EventListener
    public void handleWebSocketDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Long empId = resolveEmpId(accessor);
        if (empId == null) return;

        msngrService.updatePtcptSttusById(empId, ParticipantStatus.OFFLINE);
        log.info("WebSocket 끊김 - empId: {}, 상태: {}", empId, ParticipantStatus.OFFLINE.getCode());
    }

    private Long resolveEmpId(StompHeaderAccessor accessor) {
        if (accessor.getUser() != null) {
            return Long.valueOf(accessor.getUser().getName());
        }

        String token = extractToken(accessor);
        return token == null ? null : jwtTokenProvider.getEmpId(token);
    }

    /**
     * StompHeaderAccessor에서 JWT 토큰을 추출한다.
     * Authorization 헤더가 없거나 형식이 맞지 않으면 null을 반환한다.
     */
    private String extractToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
