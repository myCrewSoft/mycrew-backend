package com.mycrewsoft.domain.messenger.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "채팅 메시지 응답 DTO")
@Getter
@Builder
public class ChatMessageResponse {

    @Schema(description = "메시지 ID", example = "101")
    private Long id;

    @Schema(description = "채팅방 ID", example = "1")
    private Long chatRoomId;

    @Schema(description = "보낸 사람 ID", example = "1236")
    private Long senderId;
    
    @Schema(description = "보낸 사람 이름", example = "홍길동")
    private String senderName;

    @Schema(description = "메시지 내용", example = "내일 회의 몇 시예요?")
    private String content;

    @Schema(description = "보낸 시간 (HH:mm 형식)", example = "09:15")
    private String time;

    @Setter
    @Schema(description = "내가 보낸 메시지 여부 (true: 내 메시지)", example = "false")
    private Boolean mine;

    @Setter
    @Schema(description = "읽음 여부 (true: 읽음)", example = "true")
    private Boolean read;

    @Setter
    @Schema(description = "안 읽은 사람 수", example = "2")
    private Integer unreadCount;
}
