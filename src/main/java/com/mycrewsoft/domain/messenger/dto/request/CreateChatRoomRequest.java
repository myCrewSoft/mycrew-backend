package com.mycrewsoft.domain.messenger.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Schema(description = "채팅방 생성 요청 DTO")
@Getter
@Setter
public class CreateChatRoomRequest {

    @Schema(description = "그룹 채팅방 이름 (1:1 채팅에서는 생략 가능)", example = "프론트엔드팀")
    private String chatName;

    @Schema(description = "채팅방 설명", example = "프론트엔드 개발 전용 채팅방입니다.")
    private String chatDescription;

    @Schema(description = "채팅방 이미지 첨부파일 ID", example = "10", nullable = true)
    private Long chatRoomImageAtchFileId;

    @NotEmpty(message = "참여자를 1명 이상 선택해야 합니다.")
    @Schema(description = "참여자 회원 ID 목록. 1명이면 1:1, 2명 이상이면 그룹으로 처리", example = "[2, 5, 8]")
    private List<Long> participantIds;
}
