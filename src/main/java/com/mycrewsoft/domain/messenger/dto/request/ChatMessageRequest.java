package com.mycrewsoft.domain.messenger.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "웹소켓 메시지 전송 요청 DTO")
@Getter
@Setter
public class ChatMessageRequest {

    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String content;
}