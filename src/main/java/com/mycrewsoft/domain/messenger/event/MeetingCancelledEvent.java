package com.mycrewsoft.domain.messenger.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 회의 취소 알림
@Getter
@RequiredArgsConstructor
public class MeetingCancelledEvent {
    private final String meetingNm;
    private final List<Long> empIds;
}
