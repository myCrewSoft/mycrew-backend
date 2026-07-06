package com.mycrewsoft.domain.schedule.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 일정 시작 임박 알림
@Getter
@RequiredArgsConstructor
public class ScheduleReminderEvent {
    private final Long schdId;
    private final String schdNm;
    private final List<Long> empIds;    
}
