package com.mycrewsoft.domain.task.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 업무 상태 변경 알림
@Getter
@RequiredArgsConstructor
public class TaskStatusChangedEvent {
    private final String taskNm;
    private final List<Long> rcvrEmpIds;    
}
