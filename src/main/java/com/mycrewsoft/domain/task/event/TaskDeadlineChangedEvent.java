package com.mycrewsoft.domain.task.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 업무 마감일 변경 알림
@Getter
@RequiredArgsConstructor
public class TaskDeadlineChangedEvent {
    private final String taskNm;
    private final List<Long> rcvrEmpIds;    
}
