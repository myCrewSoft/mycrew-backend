package com.mycrewsoft.domain.approval.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.approval.approvaldocVO.ApprovalDocVO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;
import com.mycrewsoft.domain.approval.mapper.ApprovalDraftMapper;
import com.mycrewsoft.domain.approval.mapper.DTOtoVOMapper;
import com.mycrewsoft.security.util.SecurityUtil;

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

        Long empId = SecurityUtil.getCurrentEmpId();
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
        Long empId = SecurityUtil.getCurrentEmpId();
        ApprovalDocVO savedDoc = support.requireDocForUpdate(drftDocSn);
        support.assertDrafter(savedDoc, empId);
        support.assertUpdatePermission(empId);
        support.assertDocStatus(savedDoc, ApprovalConstants.DOC_STATUS_TEMPORARY);
        support.replaceApprovalLine(empId, drftDocSn, approvalSteps, LocalDateTime.now());
    }

    @Transactional
    public void deleteTemporaryDraft(Long drftDocSn) {
        Long empId = SecurityUtil.getCurrentEmpId();
        ApprovalDocVO savedDoc = support.requireDocForUpdate(drftDocSn);
        // 기안자 본인 + 결재 기안 삭제 권한 보유 + 임시저장 상태 문서만 삭제 가능
        support.assertDrafter(savedDoc, empId);
        support.assertDeletePermission(empId);
        support.assertDocStatus(savedDoc, ApprovalConstants.DOC_STATUS_TEMPORARY);

        // 자식 데이터(결재선/단계/첨부)부터 제거한 뒤 원본 문서를 하드 삭제한다.
        approvalDraftMapper.deleteApprovalLinesByDocSn(drftDocSn);
        approvalDraftMapper.deleteApprovalStepsByDocSn(drftDocSn);
        approvalDraftMapper.deleteApprovalFileByDocSn(drftDocSn);
        approvalDraftMapper.deleteApprovalDocByDocSn(drftDocSn);
    }
}
