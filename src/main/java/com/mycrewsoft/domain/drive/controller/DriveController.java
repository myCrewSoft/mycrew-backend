package com.mycrewsoft.domain.drive.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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
@Tag(name = "Drive", description = "드라이브 API")
@RestController
@RequestMapping("/drive")
@RequiredArgsConstructor
public class DriveController {

    private final DriveService driveService;
    private final FileService fileService;
    
    @Operation(summary = "폴더 생성")
    @PostMapping("/folders")
    public ResponseEntity<ApiResponse<DriveResponseDto>> createFolder(
            @Validated @RequestBody DriveFolderCreateRequestDto reqDto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("폴더가 생성되었습니다.", driveService.createFolder(reqDto)));
    }
    
    @Operation(summary = "파일 업로드")
    @PostMapping("/files")
    public ResponseEntity<ApiResponse<DriveResponseDto>> uploadFile(
            @Validated @ModelAttribute FileUploadRequestDto reqDto,
            @RequestParam(value = "prntDriveItemId", required = false) Long prntDriveItemId) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("파일이 업로드되었습니다.",
                		driveService.uploadFile(reqDto, prntDriveItemId)));
    }
    
    @Operation(summary = "개인 드라이브 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DriveResponseDto>>> getMyDriveList(){
		return ResponseEntity.ok(
			ApiResponse.success("드라이브 목록 조회 성공", driveService.getMyDriveList())
		);
    	
    }
}