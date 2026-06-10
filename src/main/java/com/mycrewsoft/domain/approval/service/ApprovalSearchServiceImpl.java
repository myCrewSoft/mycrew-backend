package com.mycrewsoft.domain.approval.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.approval.dto.response.ApprovalDocumentDetailResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDraftSummaryResponse;
import com.mycrewsoft.domain.approval.mapper.ApprovalDraftMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApprovalSearchServiceImpl implements ApprovalSearchService {

    private final ApprovalServiceSupport support;
    private final ApprovalDraftMapper approvalDraftMapper;

    @Transactional(readOnly = true)
    public ApprovalDocumentDetailResponse readApprovalDocument(Long drftDocSn) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        ApprovalDocumentDetailResponse detail = support.requireDetail(drftDocSn);
        if (!support.canReadApprovalDocument(detail, empId)) {
            throw new com.mycrewsoft.common.exception.CustomException(com.mycrewsoft.common.exception.ErrorCode.ACCESS_DENIED);
        }
        support.populateStatus(detail);
        return detail;
    }

    @Transactional(readOnly = true)
    public ApprovalDocumentDetailResponse readDraftApprovalStatus(Long drftDocSn) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        ApprovalDocumentDetailResponse detail = support.requireDetail(drftDocSn);
        if (!detail.getEmpId().equals(empId)) {
            throw new com.mycrewsoft.common.exception.CustomException(com.mycrewsoft.common.exception.ErrorCode.ACCESS_DENIED);
        }
        support.populateStatus(detail);
        return detail;
    }

    @Transactional(readOnly = true)
    public List<ApprovalDraftSummaryResponse> readMyDrafts(String documentStatus, String keyword) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        return approvalDraftMapper.selectMyDrafts(empId, documentStatus, keyword);
    }

    @Transactional(readOnly = true)
    public List<ApprovalDraftSummaryResponse> readMyApprovalRequests(String keyword) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        return approvalDraftMapper.selectMyApprovalRequests(empId, keyword);
    }

    @Transactional(readOnly = true)
    public List<ApprovalDraftSummaryResponse> readMyApprovalHistory(String keyword) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        return approvalDraftMapper.selectMyApprovalHistory(empId, keyword, null);
    }

    @Transactional(readOnly = true)
    public List<ApprovalDraftSummaryResponse> readMyCompletedApprovalDocuments(String keyword) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        return approvalDraftMapper.selectMyApprovalHistory(
                empId,
                keyword,
                ApprovalConstants.DOC_STATUS_COMPLETED
        );
    }

    @Transactional(readOnly = true)
    public List<ApprovalDraftSummaryResponse> searchApprovalDocumentsForApprover(String listType, String keyword) {
        if ("request".equalsIgnoreCase(listType)) {
            return readMyApprovalRequests(keyword);
        }
        if ("history".equalsIgnoreCase(listType)) {
            return readMyApprovalHistory(keyword);
        }
        if ("completed".equalsIgnoreCase(listType)) {
            return readMyCompletedApprovalDocuments(keyword);
        }
        throw new com.mycrewsoft.common.exception.CustomException(com.mycrewsoft.common.exception.ErrorCode.INVALID_INPUT_VALUE);
    }
}
