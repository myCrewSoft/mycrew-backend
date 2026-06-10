package com.mycrewsoft.domain.approval.service;

import java.util.List;

import com.mycrewsoft.domain.approval.dto.response.ApprovalDocumentDetailResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDraftSummaryResponse;

public interface ApprovalSearchService {
    ApprovalDocumentDetailResponse readApprovalDocument(Long drftDocSn);

    ApprovalDocumentDetailResponse readDraftApprovalStatus(Long drftDocSn);

    List<ApprovalDraftSummaryResponse> readMyDrafts(String documentStatus, String keyword);

    List<ApprovalDraftSummaryResponse> readMyApprovalRequests(String keyword);

    List<ApprovalDraftSummaryResponse> readMyApprovalHistory(String keyword);

    List<ApprovalDraftSummaryResponse> readMyCompletedApprovalDocuments(String keyword);

    List<ApprovalDraftSummaryResponse> searchApprovalDocumentsForApprover(String listType, String keyword);
}
