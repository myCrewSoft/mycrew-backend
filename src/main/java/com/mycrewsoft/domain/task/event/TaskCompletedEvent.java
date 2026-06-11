package com.mycrewsoft.domain.task.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 업무 완료 알림
@Getter
@RequiredArgsConstructor
public class TaskCompletedEvent {
    private final String taskNm;
    private final List<Long> rcvrEmpIds;    
}
