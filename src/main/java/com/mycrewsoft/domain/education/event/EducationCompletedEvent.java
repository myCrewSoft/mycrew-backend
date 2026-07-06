package com.mycrewsoft.domain.education.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 교육 대상자 등록 알림
@Getter
@RequiredArgsConstructor
public class EducationCompletedEvent {
    private final String eduNm;
    private final Long rcvrEmpId;  
}
