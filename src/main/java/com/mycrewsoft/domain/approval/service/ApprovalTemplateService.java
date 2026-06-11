package com.mycrewsoft.domain.approval.service;

import java.util.List;

import com.mycrewsoft.domain.approval.dto.request.ApprovalTemplateCreateRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalUpdateRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalMutationResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalTemplateResponse;

public interface ApprovalTemplateService {
	ApprovalTemplateResponse createTemplate(ApprovalTemplateCreateRequestDTO requestDTO);
			
    ApprovalTemplateResponse loadTemplate(String tmplatCd);

    ApprovalMutationResponse toggleTemplateFavorite(String tmplatCd);
    
    List<ApprovalTemplateResponse> loadTemplates();
    
    void updateTemplates(ApprovalUpdateRequestDTO request);
}
