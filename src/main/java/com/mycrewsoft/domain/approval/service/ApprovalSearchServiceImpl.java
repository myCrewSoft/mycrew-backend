package com.mycrewsoft.domain.approval.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDocumentDetailResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDraftCountResponse;
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
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        support.populateStatus(detail);
        return detail;
    }

    @Transactional(readOnly = true)
    public ApprovalDocumentDetailResponse readDraftApprovalStatus(Long drftDocSn) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        ApprovalDocumentDetailResponse detail = support.requireDetail(drftDocSn);
        if (!detail.getEmpId().equals(empId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        support.populateStatus(detail);
        return detail;
    }

    @Transactional(readOnly = true)
    public ApprovalDraftCountResponse readMyDraftCounts() {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        return ApprovalDraftCountResponse.builder()
                .sentProgress(approvalDraftMapper.countMyDrafts(empId, ApprovalConstants.DOC_STATUS_IN_PROGRESS, null))
                .sentCompleted(approvalDraftMapper.countMyDrafts(empId, ApprovalConstants.DOC_STATUS_COMPLETED, null))
                .sentRejected(approvalDraftMapper.countMyDrafts(empId, ApprovalConstants.DOC_STATUS_REJECTED, null))
                .sentTemporary(approvalDraftMapper.countMyDrafts(empId, ApprovalConstants.DOC_STATUS_TEMPORARY, null))
                .receivedRequests(approvalDraftMapper.countMyApprovalRequests(empId, null))
                .receivedHistory(approvalDraftMapper.countMyApprovalHistory(empId, null, null))
                .receivedCompleted(approvalDraftMapper.countMyCompletedApprovalDocuments(empId, null))
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ApprovalDraftSummaryResponse> readMyDrafts(String documentStatus, String keyword, int page, int size) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        int offset = page * size;
        List<ApprovalDraftSummaryResponse> content =
                approvalDraftMapper.selectMyDrafts(empId, documentStatus, keyword, offset, size);
        long totalCount = approvalDraftMapper.countMyDrafts(empId, documentStatus, keyword);
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(content, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalDraftSummaryResponse> readMyApprovalRequests(String keyword, int page, int size) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        int offset = page * size;
        List<ApprovalDraftSummaryResponse> content =
                approvalDraftMapper.selectMyApprovalRequests(empId, keyword, offset, size);
        long totalCount = approvalDraftMapper.countMyApprovalRequests(empId, keyword);
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(content, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalDraftSummaryResponse> readMyApprovalHistory(String keyword, int page, int size) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        int offset = page * size;
        List<ApprovalDraftSummaryResponse> content =
                approvalDraftMapper.selectMyApprovalHistory(empId, keyword, null, offset, size);
        long totalCount = approvalDraftMapper.countMyApprovalHistory(empId, keyword, null);
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(content, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalDraftSummaryResponse> readMyCompletedApprovalDocuments(String keyword, int page, int size) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        int offset = page * size;
        List<ApprovalDraftSummaryResponse> content =
                approvalDraftMapper.selectMyCompletedApprovalDocuments(empId, keyword, offset, size);
        long totalCount = approvalDraftMapper.countMyCompletedApprovalDocuments(empId, keyword);
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(content, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalDraftSummaryResponse> searchApprovalDocumentsForApprover(String listType, String keyword, int page, int size) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        int offset = page * size;
        List<ApprovalDraftSummaryResponse> content;
        long totalCount;

        switch (listType) {
            case "request":
                content = approvalDraftMapper.selectMyApprovalRequests(empId, keyword, offset, size);
                totalCount = approvalDraftMapper.countMyApprovalRequests(empId, keyword);
                break;
            case "history":
                content = approvalDraftMapper.selectMyApprovalHistory(empId, keyword, null, offset, size);
                totalCount = approvalDraftMapper.countMyApprovalHistory(empId, keyword, null);
                break;
            case "completed":
                content = approvalDraftMapper.selectMyCompletedApprovalDocuments(empId, keyword, offset, size);
                totalCount = approvalDraftMapper.countMyCompletedApprovalDocuments(empId, keyword);
                break;
            default:
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(content, pageable, totalCount);
    }
}
