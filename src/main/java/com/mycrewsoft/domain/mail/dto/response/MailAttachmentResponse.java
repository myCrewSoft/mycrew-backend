package com.mycrewsoft.domain.mail.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "메일 첨부파일 응답")
public class MailAttachmentResponse {

    @Schema(description = "첨부파일 ID", example = "100")
    private Long attachmentId;

    @Schema(description = "원본 파일명", example = "회의자료.pdf")
    private String originalFileName;

    @Schema(description = "파일 크기(byte)", example = "102400")
    private Long fileSize;

    @Schema(description = "콘텐츠 타입", example = "application/pdf")
    private String contentType;
}
