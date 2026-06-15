package com.mycrewsoft.domain.approval.service;

import java.util.List;

import com.mycrewsoft.domain.approval.dto.request.ApprovalDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;

public interface ApprovalDraftWriteService {
    Long saveTemporaryDraft(ApprovalDraftRequestDTO request);

    void saveApprovalLine(Long drftDocSn, List<ApprovalStepRequestDTO> approvalSteps);

    void deleteTemporaryDraft(Long drftDocSn);
}
