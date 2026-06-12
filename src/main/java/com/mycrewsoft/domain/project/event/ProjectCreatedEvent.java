package com.mycrewsoft.domain.project.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 프로젝트 등록 이벤트
@Getter
@RequiredArgsConstructor
public class ProjectCreatedEvent {
	private final Long projId;
    private final String ProjNm;
    private final Long crtrId;
    private final List<Long> EmpIds;
}
