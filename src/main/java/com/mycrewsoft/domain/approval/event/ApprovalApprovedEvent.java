package com.mycrewsoft.domain.approval.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 결제 승인 알림
@Getter
@RequiredArgsConstructor
public class ApprovalApprovedEvent {
    private final String ApprovalNm;
    private final Long RcvrEmpId;
}
