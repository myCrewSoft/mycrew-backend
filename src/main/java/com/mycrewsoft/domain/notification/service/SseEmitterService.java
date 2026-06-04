package com.mycrewsoft.domain.notification.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SseEmitterService {

    // SSE 타임아웃 시간: 30분
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    // 현재 SSE 연결 중인 사용자 목록
    // 멀티스레드 환경에서 동시 접근이 발생할 수 있어 ConcurrentHashMap 사용
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // SSE 구독 요청
    public SseEmitter subscribe(Long empId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> remove(empId));
        emitter.onTimeout(() -> remove(empId));
        emitter.onError(e -> remove(empId));

        emitters.put(empId, emitter);

        sendDummyEvent(empId, emitter);

        return emitter;
    }

    // 특정 사용자에게 SSE 이벤트 전송
    public void send(Long empId, Object data) {
        SseEmitter emitter = emitters.get(empId);
        if (emitter == null) {
            return;
        }

        try {
            emitter.send(
                SseEmitter.event()
                    .name("notification")
                    .data(data)
            );
        } catch (IOException e) {
            log.warn("SSE 전송 실패 empId={}", empId);
            remove(empId);
        }
    }

    // 해당 사원 삭제
    public void remove(Long empId) {
        emitters.remove(empId);
    }

    // SSE 연결 직후 더미 이벤트 전송
    private void sendDummyEvent(Long empId, SseEmitter emitter) {
        try {
            emitter.send(
                SseEmitter.event()
                    .name("connect")
                    .data("connected")
            );
        } catch (IOException e) {
            log.warn("SSE 더미 이벤트 전송 실패 empId={}", empId);
            remove(empId);
        }
    }
}