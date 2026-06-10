package com.mycrewsoft.domain.video.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface VideoRcrdgFileService {

    // 녹취록 파일 업로드 후 ATCH_FILE_ID 반환
    Long upload(MultipartFile file);

    // 녹취록 파일 스트리밍 (vconfId로 조회)
    Resource stream(Long atchFileId);
}