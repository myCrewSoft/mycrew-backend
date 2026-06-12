package com.mycrewsoft.domain.project.event;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 프로젝트 등록 이벤트
@Getter
@RequiredArgsConstructor
public class ProjectCreatedEvent {
	private final Long projId;
    private final String projNm;
    private final Long crtrId;
    private final LocalDate projBgngYmd;
    private final LocalDate projEndYmd;
    private final List<Long> empIds;
}
