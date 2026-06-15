package com.mycrewsoft.domain.project.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 프로젝트 담당자 변경 알림
@Getter
@RequiredArgsConstructor
public class ProjectManagerChangedEvent {
    private final String projNm;
    private final String managerNm;
    private final List<Long> empIds;

}
