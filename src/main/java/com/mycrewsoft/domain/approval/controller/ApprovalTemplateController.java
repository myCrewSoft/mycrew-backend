package com.mycrewsoft.domain.approval.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.approval.dto.request.ApprovalTemplateCreateRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalUpdateRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalMutationResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalTemplateResponse;
import com.mycrewsoft.domain.approval.service.ApprovalTemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Approval", description = "전자결재 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/approval")
public class ApprovalTemplateController {
	private final ApprovalTemplateService approvalTemplateService;
	
	@Operation(summary = "결재 템플릿 생성", description = "HTML 양식을 포함한 결재 템플릿을 생성합니다. 템플릿 코드는 서버에서 자동 생성됩니다.")
	@PostMapping("/templates")
	public ResponseEntity<ApiResponse<ApprovalTemplateResponse>> createTemplate(
			@RequestBody @Valid ApprovalTemplateCreateRequestDTO requestDTO) {
		return ResponseEntity.ok(ApiResponse.success(approvalTemplateService.createTemplate(requestDTO)));
	}
	
	@Operation(summary = "결재 템플릿 목록 불러오기", description = "현재 사용자가 사용 가능한 결재 템플릿의 목록을 조회합니다.")
	@GetMapping("/templates")
	public ResponseEntity<ApiResponse<List<ApprovalTemplateResponse>>> loadTemplates() {
		return ResponseEntity.ok(ApiResponse.success(approvalTemplateService.loadTemplates()));
	}
	
	@Operation(summary = "결재 템플릿 수정", description = "사원이 템플릿 양식을 수정합니다.")
	@PatchMapping("/templates")
	public ResponseEntity<ApiResponse<String>> modifyTemplates(
			@RequestBody @Valid ApprovalUpdateRequestDTO requestDTO) {
		approvalTemplateService.updateTemplates(requestDTO);
		
		return ResponseEntity.ok(ApiResponse.success("수정이 완료되었습니다."));
	}
	
	@Operation(summary = "결재 템플릿 상세 조회", description = "특정 템플릿의 HTML 내용을 포함하여 조회합니다.")
	@GetMapping("/templates/{tmplatCd}")
	public ResponseEntity<ApiResponse<ApprovalTemplateResponse>> readTemplate(@PathVariable String tmplatCd) {
		return ResponseEntity.ok(ApiResponse.success(approvalTemplateService.loadTemplate(tmplatCd)));
	}

	@Operation(summary = "결재 템플릿 즐겨찾기 변경", description = "로그인한 사용자의 결재 템플릿 즐겨찾기를 등록하거나 해제합니다.")
	@PatchMapping("/templates/{tmplatCd}/favorite")
	public ResponseEntity<ApiResponse<ApprovalMutationResponse>> toggleTemplateFavorite(@PathVariable String tmplatCd) {
		return ResponseEntity.ok(ApiResponse.success(approvalTemplateService.toggleTemplateFavorite(tmplatCd)));
	}

	@Operation(
			summary = "결재 템플릿 삭제",
			description = "결재 양식을 삭제합니다. 양식 제작자 또는 '결재 양식 삭제' 권한(Global)을 가진 관리자만 삭제할 수 있습니다."
	)
	@DeleteMapping("/templates/{tmplatCd}")
	public ResponseEntity<ApiResponse<String>> deleteTemplate(@PathVariable String tmplatCd) {
		approvalTemplateService.deleteTemplate(tmplatCd);
		return ResponseEntity.ok(ApiResponse.success("결재 양식이 삭제되었습니다."));
	}
}
