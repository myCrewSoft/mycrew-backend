package com.mycrewsoft.domain.approval.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 결제 반려 알림
@Getter
@RequiredArgsConstructor
public class ApprovalRejectedEvent {
    private final String ApprovalNm;
    private final Long RcvrEmpId;
}
