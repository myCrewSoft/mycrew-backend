package com.mycrewsoft.domain.project.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 프로젝트 참여자 추가 이벤트
@Getter
@RequiredArgsConstructor
public class ProjectMembersAddedEvent {

    private final Long projId;
    private final String projNm;
    private final Long chtrmId;
    private final List<Long> empIds;
}