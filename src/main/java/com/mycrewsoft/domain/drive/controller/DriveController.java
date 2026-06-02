package com.mycrewsoft.domain.drive.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.drive.dto.DriveFolderCreateRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveResponseDto;
import com.mycrewsoft.domain.drive.service.DriveService;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.service.FileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Drive", description = "Drive API")
@RestController
@RequestMapping("/drive")
@RequiredArgsConstructor
public class DriveController {

    private final DriveService driveService;
    private final FileService fileService;

    @Operation(summary = "Create folder")
    @PostMapping("/folders")
    public ResponseEntity<ApiResponse<DriveResponseDto>> createFolder(
            @Validated @RequestBody DriveFolderCreateRequestDto reqDto) {
        return ResponseEntity.ok(ApiResponse.success("Folder created.", driveService.createFolder(reqDto)));
    }

    @Operation(summary = "Upload file")
    @PostMapping("/files")
    public ResponseEntity<ApiResponse<DriveResponseDto>> uploadFile(
            @Validated @ModelAttribute FileUploadRequestDto reqDto,
            @RequestParam(value = "prntDriveItemId", required = false) Long prntDriveItemId) {
        return ResponseEntity.ok(ApiResponse.success("File uploaded.",
                driveService.uploadFile(reqDto, prntDriveItemId)));
    }

    @Operation(summary = "Get my drive list")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DriveResponseDto>>> getMyDriveList() {
        return ResponseEntity.ok(ApiResponse.success("Drive list loaded.", driveService.getMyDriveList()));
    }
}
