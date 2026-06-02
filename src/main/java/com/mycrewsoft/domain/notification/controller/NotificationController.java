package com.mycrewsoft.domain.notification.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.notification.dto.response.NotificationResponse;
import com.mycrewsoft.domain.notification.dto.response.NotificationUnreadCountResponse;
import com.mycrewsoft.domain.notification.service.NotificationService;
import com.mycrewsoft.domain.notification.service.SseEmitterService;
import com.mycrewsoft.security.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Notifications", description = "알림 관련 API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    /**
     * SSE 구독 엔드포인트
     * produces = TEXT_EVENT_STREAM_VALUE 를 명시해야
     * Spring이 SSE 응답으로 처리한다.
     */
    @Operation(summary = "SSE 구독", description = "실시간 알림 수신을 위한 SSE 연결을 수립합니다.")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        Long empId = SecurityUtil.getCurrentEmpId();
        return sseEmitterService.subscribe(empId);
    }

    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 알림 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> readAlrmList() {

        List<NotificationResponse> notificationList = notificationService.readAlrmList();

        return ResponseEntity.ok(ApiResponse.success(notificationList));
    }

    @Operation(summary = "미읽음 알림 개수 조회", description = "로그인한 사용자의 미읽음 알림 개수를 조회합니다.")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<NotificationUnreadCountResponse>> readUnreadCount() {

        NotificationUnreadCountResponse countResponse = notificationService.readUnreadCount();

        return ResponseEntity.ok(ApiResponse.success(countResponse));
    }

    @Operation(summary = "알림 전체 읽음 처리", description = "로그인한 사용자의 미읽음 알림을 전체 읽음 처리합니다.")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> readAllAlrm() {

        notificationService.readAllAlrm();

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "알림 삭제", description = "알림을 논리 삭제합니다. 본인 알림이 아닌 경우 처리되지 않습니다.")
    @DeleteMapping("/{alrmRcvrId}")
    public ResponseEntity<ApiResponse<Void>> deleteAlrm(@PathVariable Long alrmRcvrId) {

        notificationService.deleteAlrm(alrmRcvrId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
