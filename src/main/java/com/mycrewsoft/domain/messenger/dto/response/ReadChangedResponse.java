package com.mycrewsoft.domain.messenger.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "채팅 메시지 읽음 상태 변경 WebSocket 응답 DTO")
public class ReadChangedResponse {

    @Schema(description = "채팅방 ID", example = "1")
    private Long chatRoomId;

    @Schema(description = "읽음 처리한 사원 ID", example = "1001")
    private Long empId;

    @Schema(description = "읽음 수가 변경된 메시지 ID", example = "101")
    private Long messageId;

    @Schema(description = "마지막으로 확인한 메시지 ID", example = "101")
    private Long lastCfmtnMsgId;

    @Schema(description = "해당 메시지를 아직 읽지 않은 참여자 수", example = "2")
    private Integer unreadCount;
}