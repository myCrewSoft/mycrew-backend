package com.mycrewsoft.domain.schedule.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 일정 변경 알림
@Getter
@RequiredArgsConstructor
public class ScheduleChangedEvent {
    private final String schdNm;
    private final List<Long> empIds;    
}
