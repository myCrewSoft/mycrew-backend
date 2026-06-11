package com.mycrewsoft.domain.task.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 업무 담당자 변경 알림
@Getter
@RequiredArgsConstructor
public class TaskManagerChangedEvent {
    private final String taskNm;
    private final String managerNm;
    private final List<Long> rcvrEmpIds;    
}
