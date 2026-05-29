package com.mycrewsoft.domain.drive.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.drive.dto.DriveFolderCreateRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveResponseDto;
import com.mycrewsoft.domain.drive.service.DriveService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Drive", description = "드라이브 API")
@RestController
@RequestMapping("/api/v1/drive")
@RequiredArgsConstructor
public class DriveController {

    private final DriveService driveService;

    @Operation(summary = "폴더 생성")
    @PostMapping("/folders")
    public ResponseEntity<ApiResponse<DriveResponseDto>> createFolder(
            @Validated @RequestBody DriveFolderCreateRequestDto reqDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("폴더가 생성되었습니다.", driveService.createFolder(reqDto)));
    }
    
}