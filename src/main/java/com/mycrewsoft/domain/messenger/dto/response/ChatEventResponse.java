package com.mycrewsoft.domain.messenger.dto.response;

import com.mycrewsoft.domain.messenger.enums.ChatEventType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatEventResponse<T> {

    private ChatEventType eventType;
    private T data;
}