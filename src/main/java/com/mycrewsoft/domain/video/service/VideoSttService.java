package com.mycrewsoft.domain.video.service;

import org.springframework.web.multipart.MultipartFile;

public interface VideoSttService {

    // 오디오를 Whisper API로 변환 후 대화 로그 저장
    String transcribeAndSave(Long vconfId, MultipartFile audioChunk);
}