package com.mycrewsoft.domain.messenger.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "채팅방 참여자 응답 DTO")
public class ChatParticipantResponse {

    @Schema(description = "사원 ID", example = "1001")
    private Long empId;

    @Schema(description = "사원명", example = "홍길동")
    private String empNm;

    @Schema(description = "부서명", example = "개발팀")
    private String deptNm;

    @Schema(description = "직급명", example = "대리")
    private String jobGrdNm;

    @Schema(description = "프로필 이미지 첨부파일 ID", example = "10")
    private Long prflImgFileId;

    @Schema(description = "참여자 상태 코드", example = "STS1")
    private String ptcptSttusCd;
}
