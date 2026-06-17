package com.mycrewsoft.domain.approval.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApprovalAiChatClientImpl implements ApprovalAiChatClient {

    private final ChatClient chatClient;

    @Override
    public String complete(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
