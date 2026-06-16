package com.mycrewsoft.domain.education.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 교육 취소 알림
@Getter
@RequiredArgsConstructor
public class EducationCancelledEvent {
    private final String eduNm;
    private final List<Long> rcvrEmpIds;      
}
