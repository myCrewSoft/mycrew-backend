package com.mycrewsoft.domain.approval.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 결제 마감 임박 알림
@Getter
@RequiredArgsConstructor
public class ApprovalDeadlineEvent {
    private final String ApprovalNm;
    private final Long ApproverId;
    private final Long drftDocSn;
}
