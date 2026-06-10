package com.mycrewsoft.domain.approval.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.approval.approvaldocVO.ApprovalDocVO;
import com.mycrewsoft.domain.approval.approvalstepVO.ApprovalStepVO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;
import com.mycrewsoft.domain.approval.mapper.ApprovalDraftMapper;
import com.mycrewsoft.domain.approval.mapper.DTOtoVOMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApprovalDraftWriteServiceImpl implements ApprovalDraftWriteService {

    private final ApprovalServiceSupport support;
    private final DTOtoVOMapper dtoToVOMapper;
    private final ApprovalDraftMapper approvalDraftMapper;

    @Transactional
    public Long saveTemporaryDraft(ApprovalDraftRequestDTO request) {
        support.assertCreatePermission();

        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        LocalDateTime now = LocalDateTime.now();
        ApprovalDocVO approvalDoc = dtoToVOMapper.toApprovalDocVO(
                request,
                empId,
                now,
                ApprovalConstants.DOC_STATUS_TEMPORARY
        );
        approvalDoc.setDrftDocSn(request.getDrftDocSn());
        approvalDoc.setAprvlFullCn(support.normalizeClobForTemporarySave(request.getAprvlFullCn()));

        if (request.getDrftDocSn() == null) {
            approvalDraftMapper.insertApprovalDoc(approvalDoc);
        } else {
            ApprovalDocVO savedDoc = support.requireDocForUpdate(request.getDrftDocSn());
            support.assertDrafter(savedDoc, empId);
            support.assertDocStatus(savedDoc, ApprovalConstants.DOC_STATUS_TEMPORARY);
            approvalDraftMapper.updateApprovalDocTemporary(approvalDoc);
        }

        support.replaceApprovalFile(approvalDoc.getDrftDocSn(), request.getAtchFileId());
        if (request.getApprovalLines() != null) {
            support.replaceApprovalLine(empId, approvalDoc.getDrftDocSn(), request.getApprovalLines(), now);
        }

        return approvalDoc.getDrftDocSn();
    }

    @Transactional
    public void saveApprovalLine(Long drftDocSn, List<ApprovalStepRequestDTO> approvalSteps) {
        Long empId = com.mycrewsoft.security.util.SecurityUtil.getCurrentEmpId();
        ApprovalDocVO savedDoc = support.requireDocForUpdate(drftDocSn);
        support.assertDrafter(savedDoc, empId);
        support.assertDocStatus(savedDoc, ApprovalConstants.DOC_STATUS_TEMPORARY);
        support.replaceApprovalLine(empId, drftDocSn, approvalSteps, LocalDateTime.now());
    }
}
