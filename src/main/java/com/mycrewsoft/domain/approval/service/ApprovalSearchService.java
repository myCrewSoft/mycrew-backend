package com.mycrewsoft.domain.approval.service;

import org.springframework.data.domain.Page;

import com.mycrewsoft.domain.approval.dto.response.ApprovalDocumentDetailResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDraftCountResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDraftSummaryResponse;

public interface ApprovalSearchService {
    ApprovalDocumentDetailResponse readApprovalDocument(Long drftDocSn);

    ApprovalDocumentDetailResponse readDraftApprovalStatus(Long drftDocSn);

    ApprovalDraftCountResponse readMyDraftCounts();

    Page<ApprovalDraftSummaryResponse> readMyDrafts(String documentStatus, String keyword, int page, int size);

    Page<ApprovalDraftSummaryResponse> readMyApprovalRequests(String keyword, int page, int size);

    Page<ApprovalDraftSummaryResponse> readMyApprovalHistory(String keyword, int page, int size);

    Page<ApprovalDraftSummaryResponse> readMyCompletedApprovalDocuments(String keyword, int page, int size);

    Page<ApprovalDraftSummaryResponse> searchApprovalDocumentsForApprover(String listType, String keyword, int page, int size);
}
