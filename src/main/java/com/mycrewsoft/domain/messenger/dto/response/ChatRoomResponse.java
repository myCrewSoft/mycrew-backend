package com.mycrewsoft.domain.messenger.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Schema(description = "채팅방 목록/헤더 응답 DTO")
@Getter
@Builder(toBuilder = true)
public class ChatRoomResponse {

    @Schema(description = "채팅방 ID", example = "1")
    private Long id;

    @Schema(description = "채팅방 이름 또는 1:1 상대 이름", example = "김개발")
    private String name;

    @Schema(description = "채팅방 종류 (M1: 1:1, M2: 그룹, M3: 프로젝트)", example = "M1")
    private String type;

    @Schema(description = "1:1 상대 프로필 이미지 첨부파일 ID", example = "10")
    private Long prflImgFileId;

    @Schema(description = "채팅방 이미지 첨부파일 ID", example = "20", nullable = true)
    private Long chatRoomImageAtchFileId;

    @Schema(description = "채팅방 설명", example = "프론트엔드 개발팀 채팅방")
    private String description;

    @Schema(description = "마지막 메시지 내용", example = "확인했습니다!")
    private String lastMessage;

    @Schema(description = "마지막 메시지 시간 (HH:mm 또는 날짜 형식)", example = "14:32")
    private String lastTime;

    @Schema(description = "읽지 않은 메시지 수", example = "3")
    private Integer unreadCount;

    @Schema(description = "접속 상태 (STS1: 로그인, STS2: 자리비움, STS3: 다른 업무 중, STS4: 로그아웃)", example = "STS1")
    private String status;

    @Schema(description = "1:1 상대 직급", example = "대리")
    private String jobTitle;

    @Schema(description = "1:1 상대 부서", example = "개발팀")
    private String department;

    @Schema(description = "채팅방 참여자 수", example = "5")
    private Integer participantCount;

    @Schema(description = "채팅방 참여자 목록")
    private List<ChatParticipantResponse> participants;
}
