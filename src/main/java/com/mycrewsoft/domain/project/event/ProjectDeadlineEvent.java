package com.mycrewsoft.domain.project.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 프로젝트 마감 임박 알림
@Getter
@RequiredArgsConstructor
public class ProjectDeadlineEvent {
    private final Long projId;
    private final String projNm;
    private final List<Long> empIds;
}
