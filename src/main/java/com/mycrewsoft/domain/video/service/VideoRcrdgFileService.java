package com.mycrewsoft.domain.video.service;

import org.springframework.web.multipart.MultipartFile;

public interface VideoRcrdgFileService {

    // 회의 생성자 권한을 확인하고 녹취 파일과 회의 연결 정보를 함께 저장
    Long upload(Long vconfId, MultipartFile file);

    // 회의 참여 권한과 파일 연결 관계를 확인한 후 파일 응답 정보 반환
    VideoRcrdgFileResource getFile(Long vconfId, Long atchFileDtlId);
}
