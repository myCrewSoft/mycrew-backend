package com.mycrewsoft.domain.drive.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.drive.dto.DriveFolderCreateRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveRenameRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveResponseDto;
import com.mycrewsoft.domain.drive.dto.DriveSearchRequestDto;
import com.mycrewsoft.domain.drive.service.DriveService;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.service.FileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Drive", description = "Drive API")
@RestController
@RequestMapping("/drive")
@RequiredArgsConstructor
public class DriveController {

    private final DriveService driveService;
    
    @Operation(summary = "폴더 생성")
    @PostMapping("/folders")
    public ResponseEntity<ApiResponse<DriveResponseDto>> createFolder(
            @Validated @RequestBody DriveFolderCreateRequestDto reqDto) {
        return ResponseEntity.ok(ApiResponse.success("폴더 생성 성공", driveService.createFolder(reqDto)));
    }

    @Operation(summary = "파일 업로드")
    @PostMapping("/files")
    public ResponseEntity<ApiResponse<DriveResponseDto>> uploadFile(
            @Validated @ModelAttribute FileUploadRequestDto reqDto,
            @RequestParam(value = "prntDriveItemId", required = false) Long prntDriveItemId) {
        return ResponseEntity.ok(ApiResponse.success("파일 업로드 성공",
                driveService.uploadFile(reqDto, prntDriveItemId)));
    }

    @Operation(summary = "개인 드라이브 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DriveResponseDto>>> getMyDriveList(
    		@ModelAttribute DriveSearchRequestDto reqDto){
    	Page<DriveResponseDto> result = driveService.getMyDriveList(reqDto);
    	return ResponseEntity.ok(ApiResponse.success(result.getContent(), result));
    }
    
    @Operation(summary = "폴더명 수정")
    @PatchMapping("/folders/{driveItemId}/name")
    public ResponseEntity<ApiResponse<DriveResponseDto>> renameItem(
    	@PathVariable Long driveItemId,
    	@Valid @RequestBody DriveRenameRequestDto reqDto 
    ){
		return ResponseEntity.ok(
				ApiResponse.success("폴더명 수정 성공", driveService.renameItem(driveItemId, reqDto.getItemNm())));
    	
    }
    
    @Operation(summary = "즐겨찾기 등록/해제")
    @PatchMapping("/items/{driveItemId}/bookmark")
    public ResponseEntity<ApiResponse<DriveResponseDto>> registerBookmark(
    	@PathVariable Long driveItemId
    ){
    	return ResponseEntity.ok(ApiResponse.success("즐겨찾기 등록 성공", driveService.toggleBookmark(driveItemId)));
    }
    
    
    @Operation(summary = "드라이브 단건 삭제")
    @PatchMapping("/items/{driveItemId}/delete")
    public ResponseEntity<ApiResponse<String>> deleteItem(@PathVariable Long driveItemId){
    	driveService.deleteItem(driveItemId);
		return ResponseEntity.ok(ApiResponse.success("삭제되었습니다."));
    }
    
    @Operation(summary = "휴지통 목록 조회")
    @GetMapping("/trash")
    public ResponseEntity<ApiResponse<List<DriveResponseDto>>> getTrashList(){
    	return ResponseEntity.ok(ApiResponse.success("휴지통 목록 조회 성공", driveService.getTrashList()));
    }
    
    @Operation(summary = "휴지통 복원")
    @PatchMapping("/trash/{driveItemId}/restore")
    public ResponseEntity<ApiResponse<String>> restoreItem(@PathVariable Long driveItemId){
    	driveService.restoreItem(driveItemId);
    	return ResponseEntity.ok(ApiResponse.success("복원되었습니다"));
    }
    
    @Operation(summary = "파일 다운로드")
    @GetMapping("/files/{driveItemId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long driveItemId){
    	return driveService.downloadFile(driveItemId);
    }
    
    @Operation(summary = "영구 삭제")
    @DeleteMapping("/trash/{driveItemId}")
    public ResponseEntity<ApiResponse<String>> hardDeleteItem(@PathVariable Long driveItemId) {
		driveService.hardDeleteItem(driveItemId);
		return ResponseEntity.ok(ApiResponse.success("영구 삭제되었습니다."));
	}
}
