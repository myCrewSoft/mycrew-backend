package com.mycrewsoft.domain.schedule.dto.command;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskScheduleCreateCommand {

    private Long taskId;              // 업무 ID
    private String taskNm;            // 업무명

    private LocalDateTime taskBgngYmd;    // 업무 시작일
    private LocalDateTime taskEndYmd;     // 업무 종료일 또는 마감일

    private Long crtrId;              // 생성자 ID
    private List<Long> empIds;        // 업무 담당자/참여자 ID 목록
}