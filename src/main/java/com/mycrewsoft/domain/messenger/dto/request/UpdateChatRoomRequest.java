package com.mycrewsoft.domain.messenger.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "채팅방 수정 요청 DTO")
@Getter
@Setter
public class UpdateChatRoomRequest {

    @Schema(description = "그룹 채팅방 이름 (1:1 채팅에서는 생략 가능)", example = "프론트엔드팀")
    private String chatName;

    @Schema(description = "채팅방 설명", example = "프론트엔드 개발 전용 채팅방입니다.")
    private String chatDescription;

    @Schema(description = "채팅방 이미지 첨부파일 ID", example = "10", nullable = true)
    private Long chatRoomImageAtchFileId;

}
