package com.mycrewsoft.domain.approval.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.approval.approvaldocVO.ApprovalDocVO;
import com.mycrewsoft.domain.approval.approvallineVO.ApprovalLineVO;
import com.mycrewsoft.domain.approval.approvalstepVO.ApprovalStepVO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalActionRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAvailabilityResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalMutationResponse;
import com.mycrewsoft.domain.approval.event.ApprovalApprovedEvent;
import com.mycrewsoft.domain.approval.event.ApprovalCancelledEvent;
import com.mycrewsoft.domain.approval.event.ApprovalRejectedEvent;
import com.mycrewsoft.domain.approval.event.ApprovalRequestedEvent;
import com.mycrewsoft.domain.approval.mapper.ApprovalDraftMapper;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApprovalRequestServiceImpl implements ApprovalRequestService {

    private final ApprovalServiceSupport support;
    private final ApprovalDraftMapper approvalDraftMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final EmployeeMapper employeeMapper;
    
    @Transactional
    @Override
    public ApprovalMutationResponse submitApproval(Long drftDocSn) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        LocalDateTime now = LocalDateTime.now();
        ApprovalDocVO savedDoc = support.requireDocForUpdate(drftDocSn);
        support.assertDrafter(savedDoc, empId);
        support.assertDocStatus(savedDoc, ApprovalConstants.DOC_STATUS_TEMPORARY);
        support.validateSubmitApproval(savedDoc);

        approvalDraftMapper.updateAllStepsStatus(
                drftDocSn,
                ApprovalConstants.STEP_STATUS_WAITING,
                empId,
                now
        );
        approvalDraftMapper.updateAllLinesWaiting(drftDocSn, ApprovalConstants.LINE_STATUS_WAITING);

        ApprovalStepVO firstStep = approvalDraftMapper.selectFirstApprovalStep(drftDocSn);
        if (firstStep == null) {
            throw new com.mycrewsoft.common.exception.CustomException(com.mycrewsoft.common.exception.ErrorCode.APPROVAL_LINE_INVALID);
        }
        approvalDraftMapper.updateStepStatus(
                firstStep.getAprvlStepSn(),
                ApprovalConstants.STEP_STATUS_IN_PROGRESS,
                empId,
                now
        );
        approvalDraftMapper.updateDocumentSubmitted(
                drftDocSn,
                ApprovalConstants.DOC_STATUS_IN_PROGRESS,
                now
        );

        // 기안자 이름 조회 후 결재자들에게 알림
        String applicantNm = employeeMapper.selectEmployeeProfileByEmpId(empId).getEmpNm();
        List<Long> approverIds = approvalDraftMapper.selectApproverEmpIdsByStep(firstStep.getAprvlStepSn());
        eventPublisher.publishEvent(
            new ApprovalRequestedEvent(savedDoc.getDocTtl(), applicantNm, approverIds)
        );
        
        return new ApprovalMutationResponse(
                drftDocSn,
                ApprovalConstants.DOC_STATUS_IN_PROGRESS,
                "결재 요청이 완료되었습니다."
        );
    }

    @Transactional
    @Override
    public ApprovalMutationResponse withdrawApproval(Long drftDocSn) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        LocalDateTime now = LocalDateTime.now();
        ApprovalDocVO savedDoc = support.requireDocForUpdate(drftDocSn);
        support.assertDrafter(savedDoc, empId);
        support.assertDocStatus(savedDoc, ApprovalConstants.DOC_STATUS_IN_PROGRESS);
        if (approvalDraftMapper.countProcessedApprovalLines(drftDocSn) > 0) {
            throw new com.mycrewsoft.common.exception.CustomException(com.mycrewsoft.common.exception.ErrorCode.APPROVAL_ALREADY_PROCESSED);
        }

        approvalDraftMapper.updateDocumentStatus(
                drftDocSn,
                ApprovalConstants.DOC_STATUS_WITHDRAWN,
                null,
                null
        );
        approvalDraftMapper.updateAllStepsStatus(
                drftDocSn,
                ApprovalConstants.STEP_STATUS_SKIPPED,
                empId,
                now
        );
        approvalDraftMapper.updateAllLinesWaiting(
                drftDocSn,
                ApprovalConstants.LINE_STATUS_WITHDRAW_CANCELLED
        );

        // 기안자 이름 조회 후 현재 결재 대기 중인 결재자들에게 알림
        String applicantNm = employeeMapper.selectEmployeeProfileByEmpId(empId).getEmpNm();
        ApprovalStepVO currentStep = approvalDraftMapper.selectFirstApprovalStep(drftDocSn);
        if (currentStep != null) {
            List<Long> approverIds = approvalDraftMapper.selectApproverEmpIdsByStep(currentStep.getAprvlStepSn());
            eventPublisher.publishEvent(
                new ApprovalCancelledEvent(savedDoc.getDocTtl(), applicantNm, approverIds)
            );
        }
        
        return new ApprovalMutationResponse(
                drftDocSn,
                ApprovalConstants.DOC_STATUS_WITHDRAWN,
                "결재 회수가 완료되었습니다."
        );
    }

    @Transactional
    @Override
    public ApprovalMutationResponse approveApproval(Long drftDocSn, ApprovalActionRequestDTO request) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        LocalDateTime now = LocalDateTime.now();
        ApprovalDocVO savedDoc = support.requireDocForUpdate(drftDocSn);
        support.assertDocStatus(savedDoc, ApprovalConstants.DOC_STATUS_IN_PROGRESS);

        ApprovalStepVO currentStep = support.requireCurrentStep(drftDocSn);
        ApprovalLineVO myLine = support.requireMyWaitingLine(currentStep, empId);

        // 전자서명 이미지가 등록되지 않은 결재자는 승인할 수 없다.
        Long stampFileId = approvalDraftMapper.selectEmpStampFileId(empId);
        if (stampFileId == null) {
            throw new com.mycrewsoft.common.exception.CustomException(
                    com.mycrewsoft.common.exception.ErrorCode.APPROVAL_SIGNATURE_REQUIRED);
        }

        approvalDraftMapper.updateApprovalLineApproved(
                myLine.getAprvlLineSn(),
                request == null ? null : request.getReason(),
                stampFileId, // 승인 시점의 전자서명 스냅샷
                now
        );

        if (approvalDraftMapper.countApprovalLinesByStepAndStatus(
                currentStep.getAprvlStepSn(),
                ApprovalConstants.LINE_STATUS_WAITING) > 0) {
            return new ApprovalMutationResponse(
                    drftDocSn,
                    ApprovalConstants.DOC_STATUS_IN_PROGRESS,
                    "승인 처리되었습니다."
            );
        }

        approvalDraftMapper.updateStepStatus(
                currentStep.getAprvlStepSn(),
                ApprovalConstants.STEP_STATUS_COMPLETED,
                empId,
                now
        );
        ApprovalStepVO nextStep = approvalDraftMapper.selectNextApprovalStep(drftDocSn, currentStep.getAprvlOrd());
        if (nextStep != null) {
            approvalDraftMapper.updateStepStatus(
                    nextStep.getAprvlStepSn(),
                    ApprovalConstants.STEP_STATUS_IN_PROGRESS,
                    empId,
                    now
            );
            
            // 다음 단계 결재자에게 알림
            String applicantNm = employeeMapper
                .selectEmployeeProfileByEmpId(savedDoc.getEmpId()).getEmpNm();
            List<Long> nextApproverIds = approvalDraftMapper.selectApproverEmpIdsByStep(nextStep.getAprvlStepSn());
            eventPublisher.publishEvent(
                new ApprovalRequestedEvent(savedDoc.getDocTtl(), applicantNm, nextApproverIds)
            );
            
           
            return new ApprovalMutationResponse(
                    drftDocSn,
                    ApprovalConstants.DOC_STATUS_IN_PROGRESS,
                    "승인 처리되었습니다."
            );
        }

        approvalDraftMapper.updateDocumentStatus(
                drftDocSn,
                ApprovalConstants.DOC_STATUS_COMPLETED,
                now,
                null
        );
        
        // 최종 승인 시 기안자에게 알림 + 근태 취합 연동(휴가/초과근무 신청일 경우)
        eventPublisher.publishEvent(
            new ApprovalApprovedEvent(savedDoc.getDocTtl(), savedDoc.getEmpId(), savedDoc.getDrftDocSn())
        );
        
        return new ApprovalMutationResponse(
                drftDocSn,
                ApprovalConstants.DOC_STATUS_COMPLETED,
                "최종 결재가 완료되었습니다."
        );
    }

    @Transactional
    @Override
    public ApprovalMutationResponse rejectApproval(Long drftDocSn, ApprovalActionRequestDTO request) {
        if (request == null || !org.springframework.util.StringUtils.hasText(request.getReason())) {
            throw new com.mycrewsoft.common.exception.CustomException(com.mycrewsoft.common.exception.ErrorCode.APPROVAL_REJECT_REASON_REQUIRED);
        }

        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        LocalDateTime now = LocalDateTime.now();
        ApprovalDocVO savedDoc = support.requireDocForUpdate(drftDocSn);
        support.assertDocStatus(savedDoc, ApprovalConstants.DOC_STATUS_IN_PROGRESS);

        ApprovalStepVO currentStep = support.requireCurrentStep(drftDocSn);
        ApprovalLineVO myLine = support.requireMyWaitingLine(currentStep, empId);
        approvalDraftMapper.updateApprovalLineRejected(myLine.getAprvlLineSn(), request.getReason(), now);
        approvalDraftMapper.updateStepStatus(
                currentStep.getAprvlStepSn(),
                ApprovalConstants.STEP_STATUS_REJECTED,
                empId,
                now
        );
        approvalDraftMapper.updateDocumentStatus(
                drftDocSn,
                ApprovalConstants.DOC_STATUS_REJECTED,
                null,
                now
        );
        approvalDraftMapper.updateWaitingLinesInStepToSkipped(
                currentStep.getAprvlStepSn(),
                ApprovalConstants.LINE_STATUS_SKIPPED
        );
        approvalDraftMapper.updateLaterStepsToSkipped(
                drftDocSn,
                currentStep.getAprvlOrd(),
                ApprovalConstants.STEP_STATUS_SKIPPED,
                empId,
                now
        );
        approvalDraftMapper.updateWaitingLinesAfterStepToSkipped(
                drftDocSn,
                currentStep.getAprvlOrd(),
                ApprovalConstants.LINE_STATUS_SKIPPED
        );
        
        // 반려 시 기안자에게 알림
        eventPublisher.publishEvent(
            new ApprovalRejectedEvent(savedDoc.getDocTtl(), savedDoc.getEmpId())
        );
        
        return new ApprovalMutationResponse(
                drftDocSn,
                ApprovalConstants.DOC_STATUS_REJECTED,
                "반려 처리되었습니다."
        );
    }

    @Transactional(readOnly = true)
    @Override
    public ApprovalAvailabilityResponse canApprove(Long drftDocSn) {
        return support.checkApprovalAvailability(drftDocSn, "승인할 수 있습니다.");
    }

    @Transactional(readOnly = true)
    @Override
    public ApprovalAvailabilityResponse canReject(Long drftDocSn) {
        return support.checkApprovalAvailability(drftDocSn, "반려할 수 있습니다.");
    }
}
