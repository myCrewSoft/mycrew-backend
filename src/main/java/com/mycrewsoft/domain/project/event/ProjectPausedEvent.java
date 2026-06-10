package com.mycrewsoft.domain.project.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 프로젝트 일시 중지 알림
@Getter
@RequiredArgsConstructor
public class ProjectPausedEvent {
    private final String projNm;
    private final List<Long> empIds;
}
