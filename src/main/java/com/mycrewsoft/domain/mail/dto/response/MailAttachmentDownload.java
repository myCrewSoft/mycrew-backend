package com.mycrewsoft.domain.mail.dto.response;

import org.springframework.core.io.Resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 메일 첨부파일 다운로드용 응답 (스트리밍 리소스 + 메타).
 */
@Getter
@RequiredArgsConstructor
public class MailAttachmentDownload {
    private final Resource resource;
    private final String fileName;
    private final String contentType;
}
