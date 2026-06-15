package com.mycrewsoft.domain.video.controller;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.video.service.VideoConfService;
import com.mycrewsoft.domain.video.service.VideoRcrdgFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Tag(name = "녹취록", description = "화상회의 녹취록 업로드, 스트리밍, 다운로드 API")
@RestController
@RequestMapping("/video-conferences")
@RequiredArgsConstructor
public class VideoRcrdgFileController {

    private final VideoRcrdgFileService videoRcrdgFileService;
    private final VideoConfService videoConfService;

    // 업로드 (화상회의 종료 후 프론트에서 호출)
    @Operation(summary = "녹취록 업로드", description = "화상회의 종료 후 녹취 파일을 업로드하고 TB_VIDEO_RCRDG에 연결합니다.")
    @PostMapping("/{vconfId}/recordings/upload")
    public ResponseEntity<ApiResponse<Void>> uploadRcrdg(
            @PathVariable Long vconfId,
            @RequestParam("file") MultipartFile file) {
        Long atchFileId = videoRcrdgFileService.upload(file);
        videoConfService.saveRcrdg(vconfId, atchFileId);
        return ResponseEntity.ok(ApiResponse.success("녹취록이 저장되었습니다.", null));
    }

    // 스트리밍
    @Operation(summary = "녹취록 스트리밍", description = "저장된 녹취록 파일을 스트리밍합니다.")
    @GetMapping("/{vconfId}/recordings/{atchFileId}/stream")
    public ResponseEntity<Resource> streamRcrdg(
            @PathVariable Long vconfId,
            @PathVariable Long atchFileId) {
        Resource resource = videoRcrdgFileService.getResource(atchFileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.parseMediaType("audio/webm"))
                .body(resource);
    }

    // 다운로드
    @Operation(summary = "녹취록 다운로드", description = "저장된 녹취록 파일을 다운로드합니다.")
    @GetMapping("/{vconfId}/recordings/{atchFileId}/download")
    public ResponseEntity<Resource> downloadRcrdg(
            @PathVariable Long vconfId,
            @PathVariable Long atchFileId) {
        Resource resource = videoRcrdgFileService.getResource(atchFileId);
        String fileName = videoRcrdgFileService.getOriginalFileName(atchFileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("audio/webm"))
                .body(resource);
    }
}