package com.mycrewsoft.domain.video.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.video.service.VideoSttService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "STT", description = "화상회의 실시간 음성 텍스트 변환 API")
@RestController
@RequestMapping("/video-conferences")
@RequiredArgsConstructor
public class VideoSttController {

    private final VideoSttService videoSttService;

    // 오디오 청크 수신 → Whisper 변환 → 대화 로그 저장 → 텍스트 반환
    @Operation(summary = "음성 텍스트 변환", description = "5초 오디오 청크를 받아 Whisper로 변환 후 대화 로그에 저장합니다.")
    @PostMapping("/{vconfId}/stt")
    public ResponseEntity<ApiResponse<String>> transcribe(
            @PathVariable Long vconfId,
            @RequestParam("audio") MultipartFile audioChunk) {

        String text = videoSttService.transcribeAndSave(vconfId, audioChunk);
        return ResponseEntity.ok(ApiResponse.success(text));
    }
}