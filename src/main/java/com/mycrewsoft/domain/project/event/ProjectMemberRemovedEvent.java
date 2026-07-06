package com.mycrewsoft.domain.project.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 프로젝트 참여자 제거 이벤트
@Getter
@RequiredArgsConstructor
public class ProjectMemberRemovedEvent {

    private final Long projId;
    private final String projNm;
    private final Long chtrmId;
    private final Long empId;
}