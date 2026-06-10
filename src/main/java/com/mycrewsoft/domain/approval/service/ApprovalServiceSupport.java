package com.mycrewsoft.domain.approval.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.approval.approvaldocVO.ApprovalDocVO;
import com.mycrewsoft.domain.approval.approvalfileVO.ApprovalFileVO;
import com.mycrewsoft.domain.approval.approvallineVO.ApprovalLineVO;
import com.mycrewsoft.domain.approval.approvalstepVO.ApprovalStepVO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDocumentDetailResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalStepStatusResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAvailabilityResponse;
import com.mycrewsoft.domain.approval.mapper.ApprovalDraftMapper;
import com.mycrewsoft.domain.approval.mapper.DTOtoVOMapper;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApprovalServiceSupport {

    private final AuthorizationService authorizationService;
    private final DTOtoVOMapper dtoToVOMapper;
    private final ApprovalDraftMapper approvalDraftMapper;

    public void assertCreatePermission() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.APPROVAL)
                .build();
        authorizationService.assertCurrentUserPermission(PermissionCode.APPROVAL_DRAFT_CREATE, resource);
    }
    public void assertUpdatePermission(Long empId) {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.APPROVAL)
                .ownerEmpId(empId)
                .build();
        authorizationService.assertCurrentUserPermission(PermissionCode.APPROVAL_DRAFT_UPDATE, resource);
    }
    public void assertTemplateCreatePermission() {
    	ResourceContext resource = ResourceContext.builder()
    			.resourceType(ResourceType.APPROVAL)
    			.build();
    	authorizationService.assertCurrentUserPermission(PermissionCode.APPROVAL_TEMPLATE_CREATE, resource);
    }
    public void assertTemplateUpdatePermission(Long empId) {
    	ResourceContext resource = ResourceContext.builder()
    			.resourceType(ResourceType.APPROVAL)
    			.ownerEmpId(empId)
    			.build();
    	authorizationService.assertCurrentUserPermission(PermissionCode.APPROVAL_TEMPLATE_UPDATE, resource);
    }
    
    
    public void replaceApprovalFile(Long drftDocSn, Long atchFileId) {
        approvalDraftMapper.deleteApprovalFileByDocSn(drftDocSn);
        if (atchFileId != null) {
            ApprovalFileVO approvalFile = dtoToVOMapper.toApprovalFileVO(atchFileId);
            approvalFile.setDrftDocSn(drftDocSn);
            approvalDraftMapper.insertApprovalFile(approvalFile);
        }
    }

    public void replaceApprovalLine(
            Long empId,
            Long drftDocSn,
            List<ApprovalStepRequestDTO> approvalSteps,
            LocalDateTime now) {
        List<ApprovalStepRequestDTO> sortedSteps = validateApprovalLine(approvalSteps);
        approvalDraftMapper.deleteApprovalLinesByDocSn(drftDocSn);
        approvalDraftMapper.deleteApprovalStepsByDocSn(drftDocSn);
        for (ApprovalStepRequestDTO request : sortedSteps) {
            ApprovalStepVO approvalStep = dtoToVOMapper.toApprovalStepVO(
                    request,
                    empId,
                    now,
                    ApprovalConstants.STEP_STATUS_WAITING,
                    ApprovalConstants.LINE_STATUS_WAITING
            );
            approvalStep.setDrftDocSn(drftDocSn);
            approvalDraftMapper.insertApprovalStep(approvalStep);

            for (ApprovalLineVO approvalLine : approvalStep.getApprovalLines()) {
                approvalLine.setDrftDocSn(drftDocSn);
                approvalLine.setAprvlStepSn(approvalStep.getAprvlStepSn());
                approvalDraftMapper.insertApprovalLine(approvalLine);
            }
        }
    }

    public List<ApprovalStepRequestDTO> validateApprovalLine(List<ApprovalStepRequestDTO> approvalSteps) {
        if (approvalSteps == null || approvalSteps.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        Set<Long> approvalOrders = new HashSet<>();
        for (ApprovalStepRequestDTO approvalStep : approvalSteps) {
            if (approvalStep.getAprvlOrd() == null || approvalStep.getAprvlOrd() < 1) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (!approvalOrders.add(approvalStep.getAprvlOrd())) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (approvalStep.getAprvrEmpIds() == null || approvalStep.getAprvrEmpIds().isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (approvalStep.getAprvrEmpIds().stream().anyMatch(approverEmpId -> approverEmpId == null)) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (new HashSet<>(approvalStep.getAprvrEmpIds()).size() != approvalStep.getAprvrEmpIds().size()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
        List<ApprovalStepRequestDTO> sortedSteps = new ArrayList<>(approvalSteps);
        sortedSteps.sort(Comparator.comparing(ApprovalStepRequestDTO::getAprvlOrd));
        return sortedSteps;
    }

    public void validateSubmitApproval(ApprovalDocVO savedDoc) {
        if (!StringUtils.hasText(savedDoc.getDocTtl())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (!StringUtils.hasText(savedDoc.getAprvlFullCn()) || savedDoc.getAprvlFullCn().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (approvalDraftMapper.countApprovalSteps(savedDoc.getDrftDocSn()) == 0
                || approvalDraftMapper.countApprovalLines(savedDoc.getDrftDocSn()) == 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public ApprovalDocVO requireDocForUpdate(Long drftDocSn) {
        ApprovalDocVO savedDoc = approvalDraftMapper.selectApprovalDocByDocSnForUpdate(drftDocSn);
        if (savedDoc == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return savedDoc;
    }

    public ApprovalDocumentDetailResponse requireDetail(Long drftDocSn) {
        ApprovalDocumentDetailResponse detail = approvalDraftMapper.selectApprovalDocumentDetail(drftDocSn);
        if (detail == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return detail;
    }

    public ApprovalStepVO requireCurrentStep(Long drftDocSn) {
        ApprovalStepVO currentStep = approvalDraftMapper.selectCurrentApprovalStep(drftDocSn);
        if (currentStep == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return currentStep;
    }

    public ApprovalLineVO requireMyWaitingLine(ApprovalStepVO currentStep, Long empId) {
        ApprovalLineVO myLine = approvalDraftMapper.selectApprovalLineInStep(
                currentStep.getAprvlStepSn(),
                empId
        );
        if (myLine == null || !ApprovalConstants.LINE_STATUS_WAITING.equals(myLine.getAprvlPrgrsCd())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return myLine;
    }

    public ApprovalAvailabilityResponse checkApprovalAvailability(Long drftDocSn, String availableMessage) {
        Long empId = SecurityUtil.getCurrentEmpId();
        ApprovalDocVO savedDoc = approvalDraftMapper.selectApprovalDocByDocSn(drftDocSn);
        if (savedDoc == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (!ApprovalConstants.DOC_STATUS_IN_PROGRESS.equals(savedDoc.getAprvlDocSttsCd())) {
            return new ApprovalAvailabilityResponse(drftDocSn, false, "결재 진행 중인 문서가 아닙니다.");
        }
        ApprovalStepVO currentStep = approvalDraftMapper.selectCurrentApprovalStep(drftDocSn);
        if (currentStep == null) {
            return new ApprovalAvailabilityResponse(drftDocSn, false, "현재 진행 중인 결재 단계가 없습니다.");
        }
        ApprovalLineVO myLine = approvalDraftMapper.selectApprovalLineInStep(currentStep.getAprvlStepSn(), empId);
        if (myLine == null) {
            return new ApprovalAvailabilityResponse(drftDocSn, false, "현재 결재 단계의 결재자가 아닙니다.");
        }
        if (!ApprovalConstants.LINE_STATUS_WAITING.equals(myLine.getAprvlPrgrsCd())) {
            return new ApprovalAvailabilityResponse(drftDocSn, false, "이미 처리된 결재자입니다.");
        }
        return new ApprovalAvailabilityResponse(drftDocSn, true, availableMessage);
    }

    // The above method is not used by services directly; keep specific helper methods below.

    public void assertDrafter(ApprovalDocVO savedDoc, Long empId) {
        if (!empId.equals(savedDoc.getEmpId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void assertDocStatus(ApprovalDocVO savedDoc, String expectedStatus) {
        if (!expectedStatus.equals(savedDoc.getAprvlDocSttsCd())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public boolean canReadApprovalDocument(ApprovalDocumentDetailResponse detail, Long empId) {
        if (detail.getEmpId().equals(empId)) {
            return true;
        }
        if (approvalDraftMapper.existsApprovalParticipant(detail.getDrftDocSn(), empId) > 0) {
            return true;
        }
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.APPROVAL)
                .build();
        return authorizationService.canAccess(PermissionCode.ADMIN_CONSOLE_ACCESS, resource);
    }

    public String normalizeClobForTemporarySave(String value) {
        return StringUtils.hasText(value) ? value : " ";
    }

    public void populateStatus(ApprovalDocumentDetailResponse detail) {
        List<ApprovalStepStatusResponse> steps =
                approvalDraftMapper.selectApprovalStepStatuses(detail.getDrftDocSn());
        detail.setApprovalSteps(steps);
        detail.setStatusMessages(makeStatusMessages(detail, steps));
    }

    public List<String> makeStatusMessages(
            ApprovalDocumentDetailResponse detail,
            List<ApprovalStepStatusResponse> steps) {
        List<String> messages = new ArrayList<>();
        for (ApprovalStepStatusResponse step : steps) {
            String name = step.getAprvrEmpNm();
            if (ApprovalConstants.LINE_STATUS_APPROVED.equals(step.getAprvlPrgrsCd())) {
                messages.add(name + " 승인");
            } else if (ApprovalConstants.LINE_STATUS_REJECTED.equals(step.getAprvlPrgrsCd())) {
                messages.add(name + " 반려");
            } else if (ApprovalConstants.LINE_STATUS_SKIPPED.equals(step.getAprvlPrgrsCd())
                    || ApprovalConstants.LINE_STATUS_WITHDRAW_CANCELLED.equals(step.getAprvlPrgrsCd())) {
                messages.add(name + " 생략");
            } else if (ApprovalConstants.STEP_STATUS_IN_PROGRESS.equals(step.getStepPrgrsCd())) {
                messages.add(name + " 결재 중");
            } else {
                messages.add(name + " 대기");
            }
        }
        if (ApprovalConstants.DOC_STATUS_COMPLETED.equals(detail.getAprvlDocSttsCd())) {
            messages.add("결재 완료");
        } else if (ApprovalConstants.DOC_STATUS_REJECTED.equals(detail.getAprvlDocSttsCd())) {
            messages.add("결재 반려");
        } else if (ApprovalConstants.DOC_STATUS_WITHDRAWN.equals(detail.getAprvlDocSttsCd())) {
            messages.add("결재 회수");
        }
        return messages;
    }

    // Small wrapper class to allow throwing structured error in checkApprovalAvailability placeholder.
    public static class ApprovalAvailabilityWrapper extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public ApprovalAvailabilityWrapper(Long drftDocSn, boolean b, String message) {
            super(message);
        }
    }
}
