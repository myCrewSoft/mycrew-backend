package com.mycrewsoft.domain.schedule.dto.command;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeetingScheduleCreateCommand {

    private Long meetingId;               // 회의 ID 또는 화상회의 ID
    private String meetingNm;             // 회의명

    private LocalDateTime beginDt;        // 회의 시작 일시
    private LocalDateTime endDt;          // 회의 종료 일시

    private Long crtrId;                  // 생성자 ID
    private List<Long> empIds;            // 회의 참여자 ID 목록
}