package com.mycrewsoft.domain.schedule.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 일정 취소 알림
@Getter
@RequiredArgsConstructor
public class ScheduleCancelledEvent {
    private final Long schdId;
    private final String schdNm;
    private final List<Long> empIds;    
}
