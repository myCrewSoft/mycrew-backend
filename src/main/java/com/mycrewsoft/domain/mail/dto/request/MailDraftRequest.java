package com.mycrewsoft.domain.mail.dto.request;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "메일 임시보관(드래프트) 저장 요청")
public class MailDraftRequest {

    @Schema(description = "수정할 드래프트 메일 ID. 신규 저장 시 비웁니다.", example = "1001")
    private Long mailId;

    @Schema(description = "수신자 이메일 목록")
    private List<String> to = new ArrayList<>();

    @Schema(description = "참조 이메일 목록")
    private List<String> cc = new ArrayList<>();

    @Schema(description = "숨은참조 이메일 목록")
    private List<String> bcc = new ArrayList<>();

    @Schema(description = "메일 제목", example = "작성 중인 메일")
    private String subject;

    @Schema(description = "메일 본문(HTML 허용)")
    private String content;
}
