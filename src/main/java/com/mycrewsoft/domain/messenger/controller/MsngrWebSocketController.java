package com.mycrewsoft.domain.messenger.controller;

import com.mycrewsoft.domain.messenger.dto.request.ChatMessageRequest;
import com.mycrewsoft.domain.messenger.service.MsngrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Tag(name = "Messenger WebSocket", description = "메신저 웹소켓 API")
@Controller
@RequiredArgsConstructor
public class MsngrWebSocketController {

    private final MsngrService msngrService;

    @Operation(summary = "메시지 전송")
    @MessageMapping("/chats/{chtrmId}/messages")
    public void sendMessage(
        @DestinationVariable Long chtrmId,
        @Payload ChatMessageRequest request,
        Principal principal
    ) {
        Long sndrId = Long.valueOf(principal.getName());

        msngrService.saveMsgAndBroadcast(chtrmId, request.getContent(), sndrId);
    }
}