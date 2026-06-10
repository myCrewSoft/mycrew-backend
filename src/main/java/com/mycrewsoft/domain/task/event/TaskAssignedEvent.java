package com.mycrewsoft.domain.task.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 업무 배정 알림
@Getter
@RequiredArgsConstructor
public class TaskAssignedEvent {
    private final String taskNm;
    private final List<Long> rcvrEmpIds;
}
