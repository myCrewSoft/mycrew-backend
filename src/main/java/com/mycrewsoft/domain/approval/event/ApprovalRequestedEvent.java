package com.mycrewsoft.domain.approval.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 결제 요청 알림
@Getter
@RequiredArgsConstructor
public class ApprovalRequestedEvent {
    private final String ApplicantNm;
    private final String ApprovalNm;
    private final List<Long> ApproverIds;

}
