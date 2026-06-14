package com.mycrewsoft.domain.video.event;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 회의 초대 알림
@Getter
@RequiredArgsConstructor
public class MeetingInvitedEvent {
	private final Long meetingId;
	private final String meetingNm;
	private final LocalDateTime beginDt;
	private final LocalDateTime endDt;	
    private final List<Long> empIds;
    private final Long schdWrtrId;
}
