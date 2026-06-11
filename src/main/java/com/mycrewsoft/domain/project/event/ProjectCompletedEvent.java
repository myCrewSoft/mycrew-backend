package com.mycrewsoft.domain.project.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 프로젝트 완료 알림
@Getter
@RequiredArgsConstructor
public class ProjectCompletedEvent {
    private final String projNm;
    private final List<Long> empIds;
}
