package com.mycrewsoft.domain.video.event;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 회의 일정 변경 알림
@Getter
@RequiredArgsConstructor
public class MeetingChangedEvent {
    private final String meetingNm;
    private final LocalDateTime meetingDt;
    private final List<Long> empIds;
}
