package com.mycrewsoft.domain.video.service;

import org.springframework.web.multipart.MultipartFile;

import com.mycrewsoft.domain.file.vo.FileDtlVo;

public interface VideoRcrdgFileService {

    // 녹취록 파일 업로드 후 ATCH_FILE_ID 반환
    Long upload(MultipartFile file);

    // 녹취록 파일 스트리밍 / 다운로드
    FileDtlVo getFileDtl(Long atchFileId);
}