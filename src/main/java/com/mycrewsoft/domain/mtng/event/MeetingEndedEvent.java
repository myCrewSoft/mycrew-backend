package com.mycrewsoft.domain.video.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 회의 취소 알림
@Getter
@RequiredArgsConstructor
public class MeetingEndedEvent {
    private final String meetingNm;
    private final List<Long> empIds;
    private final Long vconfId;  // AI 회의록 생성에 필요한 vconfId 추가
    private final Long mtngId;   // TB_MTNG_MOM에 저장할 mtngId 추가
}
