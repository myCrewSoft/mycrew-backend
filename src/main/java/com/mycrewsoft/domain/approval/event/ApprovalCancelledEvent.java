package com.mycrewsoft.domain.approval.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 결제 취소 알림
@Getter
@RequiredArgsConstructor
public class ApprovalCancelledEvent {
    private final String ApplicantNm;
    private final String ApprovalNm;
    private final List<Long> ApproverIds;
    private final Long drftDocSn;
}
