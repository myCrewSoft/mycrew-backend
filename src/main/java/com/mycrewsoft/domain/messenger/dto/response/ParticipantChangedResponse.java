package com.mycrewsoft.domain.messenger.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "채팅방 참여자 변경 WebSocket 응답 DTO")
public class ParticipantChangedResponse {

    @Schema(description = "채팅방 ID", example = "1")
    private Long chatRoomId;

    @Schema(description = "변경 대상 사원 ID 목록", example = "[1001, 1002]")
    private List<Long> participantIds;
}