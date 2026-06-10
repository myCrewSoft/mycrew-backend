package com.mycrewsoft.domain.video.controller;

import java.net.MalformedURLException;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.file.vo.FileDtlVo;
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

    // 스트리밍
    @Operation(summary = "녹취록 스트리밍", description = "저장된 녹취록 파일을 스트리밍합니다.")
    @GetMapping("/recordings/{atchFileId}/stream")
    public ResponseEntity<Resource> streamRcrdg(@PathVariable Long atchFileId) {
        FileDtlVo dtlVO = videoRcrdgFileService.getFileDtl(atchFileId);
        try {
            Resource resource = new UrlResource(
                Paths.get(dtlVO.getSavePathNm(), dtlVO.getSaveFileNm()).toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(MediaType.parseMediaType("audio/webm"))
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    // 다운로드
    @Operation(summary = "녹취록 다운로드", description = "저장된 녹취록 파일을 다운로드합니다.")
    @GetMapping("/recordings/{atchFileId}/download")
    public ResponseEntity<Resource> downloadRcrdg(@PathVariable Long atchFileId) {
        FileDtlVo dtlVO = videoRcrdgFileService.getFileDtl(atchFileId);
        try {
            Resource resource = new UrlResource(
                Paths.get(dtlVO.getSavePathNm(), dtlVO.getSaveFileNm()).toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + dtlVO.getOrgnlFileNm() + "\"")
                    .contentType(MediaType.parseMediaType("audio/webm"))
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }
    }
}