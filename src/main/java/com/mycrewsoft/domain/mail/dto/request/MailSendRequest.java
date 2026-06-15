package com.mycrewsoft.domain.mail.dto.request;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "메일 발송 요청")
public class MailSendRequest {

    @NotEmpty(message = "수신인은 필수입니다.")
    @Schema(description = "수신자 이메일 목록", example = "[\"user@example.com\"]")
    private List<String> to = new ArrayList<>();

    @Schema(description = "참조 이메일 목록", example = "[\"cc@example.com\"]")
    private List<String> cc = new ArrayList<>();

    @Schema(description = "숨은참조 이메일 목록", example = "[\"bcc@example.com\"]")
    private List<String> bcc = new ArrayList<>();

    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "메일 제목", example = "회의 자료 공유")
    private String subject;

    @NotBlank(message = "본문은 필수입니다.")
    @Schema(description = "메일 본문. HTML을 허용합니다.", example = "<p>자료 확인 부탁드립니다.</p>")
    private String content;

    @Schema(description = "답장/회신 대상 원본 메일 ID. 지정 시 같은 스레드로 연결됩니다.", example = "1001")
    private Long inReplyToMailId;
}
