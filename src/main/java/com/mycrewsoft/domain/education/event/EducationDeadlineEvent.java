package com.mycrewsoft.domain.education.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 교육 교육 마감 알림
@Getter
@RequiredArgsConstructor
public class EducationDeadlineEvent {
    private final String eduNm;
    private final List<Long> rcvrEmpIds;       
}
