package com.mycrewsoft.domain.file.controller;


import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.common.util.FileUtil;
import com.mycrewsoft.domain.file.constant.FileConstants;
import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.domain.file.service.FileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "파일", description = "파일 API")
public class FileController {
	private final FileService fileService;

	@Operation(summary = "게시판 첨부파일 업로드")
	@PostMapping(value = "/boards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<Long>> uploadBoardFile(
			@Valid @ModelAttribute FileUploadRequestDto reqDto) {
		Long atchFileDtlId = fileService.upload(reqDto, FileConstants.BOARD);

		return ResponseEntity.ok(
				ApiResponse.success("게시판 첨부파일 업로드 성공", atchFileDtlId));
	}
	
	@Operation(summary = "이미지 파일 서빙 (화면 표시용)")
	@GetMapping("/images/{atchFileDtlId}")
	public ResponseEntity<Resource> serveImage(@PathVariable Long atchFileDtlId){
		Resource resource = fileService.serveImage(atchFileDtlId);
		
		String contentType = FileUtil.resolveImageContentType(
			FileUtil.getExtension(resource.getFilename())
		);
		
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline")
				.body(resource);
	}
}
