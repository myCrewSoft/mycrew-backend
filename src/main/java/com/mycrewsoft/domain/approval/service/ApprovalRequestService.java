package com.mycrewsoft.domain.approval.service;

import com.mycrewsoft.domain.approval.dto.request.ApprovalActionRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAvailabilityResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalMutationResponse;

public interface ApprovalRequestService {
    ApprovalMutationResponse submitApproval(Long drftDocSn);

    ApprovalMutationResponse withdrawApproval(Long drftDocSn);

    ApprovalMutationResponse approveApproval(Long drftDocSn, ApprovalActionRequestDTO request);

    ApprovalMutationResponse rejectApproval(Long drftDocSn, ApprovalActionRequestDTO request);

    ApprovalAvailabilityResponse canApprove(Long drftDocSn);

    ApprovalAvailabilityResponse canReject(Long drftDocSn);
}
