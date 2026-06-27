package com.mycrewsoft.domain.video.service;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

/**
 * 녹취 파일 응답에 필요한 리소스와 메타데이터를 한 번에 전달한다.
 */
public record VideoRcrdgFileResource(
        Resource resource,
        String originalFileName,
        MediaType contentType) {
}
