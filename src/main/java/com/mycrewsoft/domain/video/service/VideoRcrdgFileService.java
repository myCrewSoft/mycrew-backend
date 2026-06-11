package com.mycrewsoft.domain.video.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.mycrewsoft.domain.file.vo.FileDtlVo;

public interface VideoRcrdgFileService {

    // 녹취록 파일 업로드 후 ATCH_FILE_ID 반환
    Long upload(MultipartFile file);

    // Resource 반환
    Resource getResource(Long atchFileId);       
    
    // 원본 파일명 반환
    String getOriginalFileName(Long atchFileId); 
}