package com.mycrewsoft.domain.task.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 업무 마감임박 알림
@Getter
@RequiredArgsConstructor
public class TaskDeadlineEvent {
    private final Long taskId;
    private final Long projId;
    private final String taskNm;
    private final List<Long> rcvrEmpIds;
}
