package com.mycrewsoft.domain.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.project.dto.ProjectCreateRequestDto;
import com.mycrewsoft.domain.project.service.ProjectService;
import com.mycrewsoft.validate.groups.InsertGroup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Project", description = "프로젝트 API")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
	private final ProjectService projService;
	
	@Operation(summary = "프로젝트 등록")
	@PostMapping
	public ResponseEntity<ApiResponse<String>> creatProject(
		@Validated(InsertGroup.class) @RequestBody ProjectCreateRequestDto reqDto
	){
		projService.createProject(reqDto);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("프로젝트가 생성되었습니다."));
	}
}
