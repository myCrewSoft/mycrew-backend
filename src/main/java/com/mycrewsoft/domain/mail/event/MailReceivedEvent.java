package com.mycrewsoft.domain.mail.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 새 메일 수신 알림 이벤트 */
@Getter
@RequiredArgsConstructor
public class MailReceivedEvent {
    private final Long empId;
    private final int newCount;
    private final String latestSubject;
}
