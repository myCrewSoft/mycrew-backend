package com.mycrewsoft.domain.task.event;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 업무 배정 알림
@Getter
@RequiredArgsConstructor
public class TaskAssignedEvent {
	private final Long taskId;
	private final Long projId;
	private final String taskNm;
	private final LocalDateTime taskBgngYmd;
	private final LocalDateTime taskEndYmd;
    private final List<Long> rcvrEmpIds;
    private final Long schdWrtrId;
}
