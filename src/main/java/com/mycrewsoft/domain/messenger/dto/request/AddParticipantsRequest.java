package com.mycrewsoft.domain.messenger.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "채팅방 참여자 추가 요청 DTO")
@Getter
@Setter
public class AddParticipantsRequest {
    @NotEmpty(message = "추가할 참여자를 1명 이상 선택해야 합니다.")
    @Schema(description = "추가할 참여자 사번 목록", example = "[2, 5, 8]")
    private List<Long> participantIds;
}
