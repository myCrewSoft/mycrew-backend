package com.mycrewsoft.domain.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.project.dto.ProjectCreateRequestDto;
import com.mycrewsoft.domain.project.dto.ProjectDetailResponseDto;
import com.mycrewsoft.domain.project.dto.ProjectListResponseDto;
import com.mycrewsoft.domain.project.dto.ProjectMemberAddRequest;
import com.mycrewsoft.domain.project.dto.ProjectUpdateRequestDto;
import com.mycrewsoft.domain.project.service.ProjectService;
import com.mycrewsoft.validate.groups.InsertGroup;

import io.micrometer.core.ipc.http.HttpSender.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Project", description = "프로젝트 API")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
	private final ProjectService projService;
	
	@Operation(summary = "프로젝트 등록")
	@PostMapping
	public ResponseEntity<ApiResponse<String>> creatProject(
		@Validated(InsertGroup.class) @RequestBody ProjectCreateRequestDto reqDto
	){
		projService.createProject(reqDto);
		return ResponseEntity.ok(ApiResponse.success("프로젝트가 등록되었습니다."));
	}
	
	@Operation(summary = "프로젝트 목록 조회")
	@GetMapping
	public ResponseEntity<ApiResponse<List<ProjectListResponseDto>>> getProjectList(){
		return ResponseEntity.ok(ApiResponse.success(projService.getProjectList()));
	}
	
	@Operation(summary = "프로젝트 상세 조회")
	@GetMapping("/{projId}")
	public ResponseEntity<ApiResponse<ProjectDetailResponseDto>> getProject(@PathVariable Long projId){
		return ResponseEntity.ok(ApiResponse.success(projService.getProject(projId)));
	}
	
	@Operation(summary = "프로젝트 수정")
	@PatchMapping("/{projId}")
	public ResponseEntity<ApiResponse<String>> updateProject( 
			@PathVariable Long projId,
			@RequestBody ProjectUpdateRequestDto updateReqDto){
		
		projService.modifyProject(projId, updateReqDto);
		return ResponseEntity.ok(ApiResponse.success("수정되었습니다"));
	}
	
	@Operation(summary = "프로젝트 참여자 추가")
	@PostMapping("/{projId}/ptcpts")
	public ResponseEntity<ApiResponse<String>> addProjectMember(
		@PathVariable Long projId,
		@RequestBody ProjectMemberAddRequest reqDto){
		
		projService.addProjMember(projId, reqDto);
		return ResponseEntity.ok(ApiResponse.success("참여자가 추가되었습니다"));
	}
	
	@Operation(summary = "프로젝트 참여자 단건 퇴출")
	@PutMapping("/{projId}/ptcpts")
	public ResponseEntity<ApiResponse<String>> removeProjMember(
		@PathVariable Long projId,
		@RequestBody Long empId){
		
		projService.removeProjMember(projId, empId);
		return ResponseEntity.ok(ApiResponse.success("참여자가 퇴출되었습니다."));
	}
	
}
