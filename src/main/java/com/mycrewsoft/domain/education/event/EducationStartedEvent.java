package com.mycrewsoft.domain.education.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 교육 시작 알림
@Getter
@RequiredArgsConstructor
public class EducationStartedEvent {
    private final String eduNm;
    private final List<Long> rcvrEmpIds;        
}
