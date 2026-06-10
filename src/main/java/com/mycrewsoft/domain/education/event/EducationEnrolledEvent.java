package com.mycrewsoft.domain.education.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 교육 대상자 등록 알림
@Getter
@RequiredArgsConstructor
public class EducationEnrolledEvent {
    private final String eduNm;
    private final List<Long> rcvrEmpIds;    
}
