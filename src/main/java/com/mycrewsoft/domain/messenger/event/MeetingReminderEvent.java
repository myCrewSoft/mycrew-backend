package com.mycrewsoft.domain.messenger.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 회의 시작 10분 전 알림
@Getter
@RequiredArgsConstructor
public class MeetingReminderEvent {
    private final String meetingNm;
    private final List<Long> empIds;    
}
