package com.mycrewsoft.domain.drive.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.drive.dto.DriveFolderCreateRequestDto;
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
}
