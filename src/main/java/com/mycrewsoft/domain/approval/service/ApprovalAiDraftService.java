package com.mycrewsoft.domain.approval.service;

import com.mycrewsoft.domain.approval.dto.request.ApprovalAiDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftJobResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftResponseDTO;

public interface ApprovalAiDraftService {

    ApprovalAiDraftResponseDTO createDraft(ApprovalAiDraftRequestDTO request);

    ApprovalAiDraftJobResponseDTO createDraftJob(ApprovalAiDraftRequestDTO request);

    ApprovalAiDraftJobResponseDTO getDraftJob(Long jobId);
}
