package com.mycrewsoft.domain.drive.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import com.mycrewsoft.domain.drive.service.ProjectDriveService;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/project/drive")
@RequiredArgsConstructor
@Tag(name = "프로젝트 드라이브")
public class ProjectDriveController {

	private final ProjectDriveService service;
	
	@Operation(summary = "프로젝트 드라이브 목록 조회")
	@GetMapping("{projId}")
	public ResponseEntity<ApiResponse<List<DriveResponseDto>>> getProjectDrive(
			@PathVariable Long projId,
			@ModelAttribute DriveSearchRequestDto reqDto){
		Page<DriveResponseDto> result = service.getProjectDriveList(projId, reqDto);
		return ResponseEntity.ok(ApiResponse.success(result.getContent(), result));
	}
	
	@Operation(summary = "프로젝트 드라이브 폴더 생성")
	@PostMapping("{projId}/folders")
	public ResponseEntity<ApiResponse<String>> createFolder(
			@PathVariable Long projId,
			@RequestBody DriveFolderCreateRequestDto reqdto){
		service.createFolder(projId, reqdto);
		return ResponseEntity.ok(ApiResponse.success("폴더가 생성되었습니다."));
	}
	
	@Operation(summary = "프로젝트 드라이브 파일 업로드")
	@PostMapping("{projId}/files")
	public ResponseEntity<ApiResponse<String>> uploadFile(
			@PathVariable Long projId,
			@Validated @ModelAttribute FileUploadRequestDto reqDto,
			@RequestParam(required = false) Long prntDriveItemId
	){
		service.uploadFile(projId, reqDto, prntDriveItemId);
		return ResponseEntity.ok(ApiResponse.success("파일 업로드 성공"));
	}
	
	@Operation(summary = "프로젝트 드라이브 폴더명 수정")
	@PatchMapping("/folders/{driveItemId}/name")
	public ResponseEntity<ApiResponse<String>> modifyFolderName(
		@PathVariable Long driveItemId,
		@RequestBody DriveRenameRequestDto reqDto
	){
		service.renameItem(driveItemId, reqDto);
		return ResponseEntity.ok(ApiResponse.success("폴더명이 수정되었습니다."));
	}
	
	@Operation(description = "즐겨찾기 등록/해제")
	@PatchMapping("/items/{driveItemId}/bookmark")
	public ResponseEntity<ApiResponse<String>> toggleBookmark(
		@PathVariable Long driveItemId
	){
		service.toggleBookmark(driveItemId);
		return ResponseEntity.ok(ApiResponse.success("즐겨찾기 여부가 수정되었습니다."));
	}
	
	@Operation(description = "프로젝트 드라이브 논리 삭제 (하위 포함)")
	@PatchMapping("/items/{driveItemId}/delete")
	public ResponseEntity<ApiResponse<String>> softDelete(@PathVariable Long driveItemId){
		service.softDeleteItem(driveItemId);
		return ResponseEntity.ok(ApiResponse.success("삭제되었습니다."));
	}
	
	@Operation(summary = "프로젝트 드라이브 파일 다운로드")
	@GetMapping("/files/{driveItemId}/download")
	public ResponseEntity<Resource> downloadFile(@PathVariable Long driveItemId){
		return service.downloadFile(driveItemId);
	}
}
