package com.mycrewsoft.domain.project.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 프로젝트 등록 알림
@Getter
@RequiredArgsConstructor
public class ProjectCreatedEvent {
    private final String ProjNm;
    private final List<Long> EmpIds;
}
