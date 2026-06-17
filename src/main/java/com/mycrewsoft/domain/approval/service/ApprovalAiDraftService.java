package com.mycrewsoft.domain.approval.service;

import com.mycrewsoft.domain.approval.dto.request.ApprovalAiDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalAiApprovalLineRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiApprovalLineJobResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiContentJobResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftJobResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftResponseDTO;

public interface ApprovalAiDraftService {

    ApprovalAiDraftResponseDTO createDraft(ApprovalAiDraftRequestDTO request);

    ApprovalAiDraftJobResponseDTO createDraftJob(ApprovalAiDraftRequestDTO request);

    ApprovalAiDraftJobResponseDTO getDraftJob(Long jobId);

    ApprovalAiContentJobResponseDTO createDraftContentJob(ApprovalAiDraftRequestDTO request);

    ApprovalAiContentJobResponseDTO getDraftContentJob(Long jobId);

    ApprovalAiDraftJobResponseDTO createDraftContentSaveJob(ApprovalAiDraftRequestDTO request);

    ApprovalAiDraftJobResponseDTO getDraftContentSaveJob(Long jobId);

    ApprovalAiApprovalLineJobResponseDTO createApprovalLineJob(ApprovalAiApprovalLineRequestDTO request);

    ApprovalAiApprovalLineJobResponseDTO getApprovalLineJob(Long jobId);
}
