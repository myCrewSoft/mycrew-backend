package com.mycrewsoft.domain.video.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "LiveKit 입장 토큰 응답 DTO")
@Getter
@AllArgsConstructor
public class VideoTokenResponse {

    @Schema(description = "LiveKit 방 이름")
    private String roomNm;

    @Schema(description = "LiveKit 입장 토큰")
    private String token;
}