package com.mycrewsoft.domain.video.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.video.service.VideoConfService;
import com.mycrewsoft.domain.video.service.VideoRcrdgFileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "녹취록", description = "화상회의 녹취록 파일 업로드 및 스트리밍 API")
@RestController
@RequestMapping("/video-conferences")
@RequiredArgsConstructor
public class VideoRcrdgFileController {

    private final VideoRcrdgFileService videoRcrdgFileService;
    private final VideoConfService videoConfService;

    // 녹취록 파일 업로드 후 TB_VIDEO_RCRDG에 저장
    @Operation(summary = "녹취록 업로드", description = "회의 종료 후 녹취 파일을 업로드합니다. 호스트만 가능합니다.")
    @PostMapping("/{vconfId}/recordings/upload")
    public ResponseEntity<ApiResponse<String>> uploadRcrdg(
            @PathVariable Long vconfId,
            @RequestParam("file") MultipartFile file) {

        Long atchFileId = videoRcrdgFileService.upload(file);
        videoConfService.saveRcrdg(vconfId, atchFileId);

        return ResponseEntity.ok(ApiResponse.success("녹취록이 저장되었습니다."));
    }

    // 녹취록 파일 스트리밍 (프론트 <audio> 태그로 재생)
    @Operation(summary = "녹취록 스트리밍", description = "저장된 녹취록 파일을 스트리밍합니다.")
    @GetMapping("/recordings/{atchFileId}/stream")
    public ResponseEntity<Resource> streamRcrdg(
            @PathVariable Long atchFileId) {

        Resource resource = videoRcrdgFileService.stream(atchFileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.parseMediaType("audio/webm"))
                .body(resource);
    }
}