package com.mycrewsoft.domain.messenger.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "채팅 참여자 상태 변경 WebSocket 응답 DTO")
public class ParticipantStatusResponse {

    @Schema(description = "상태가 변경된 사원 ID", example = "1001")
    private Long empId;

    @Schema(description = "참여자 상태 코드 (STS1: 온라인, STS2: 자리비움, STS3: 다른 업무 중, STS4: 오프라인)", example = "STS1")
    private String ptcptSttusCd;
}