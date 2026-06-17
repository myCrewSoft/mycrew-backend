package com.mycrewsoft.ai.chatbot.service;

import java.util.List;

import com.mycrewsoft.domain.approval.dto.request.ApprovalAiDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiApproverCandidateDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalTemplateResponse;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;

public interface ApprovalAiPromptService {

    String buildDraftContentPrompt(
            ApprovalAiDraftRequestDTO request,
            Long drafterEmpId,
            EmployeeProfileDTO drafter,
            List<ApprovalTemplateResponse> templates);

    String buildApprovalLinePrompt(
            ApprovalAiDraftRequestDTO request,
            Long drafterEmpId,
            EmployeeProfileDTO drafter,
            String docTitle,
            String templateCode,
            List<ApprovalAiApproverCandidateDTO> candidates);
}
