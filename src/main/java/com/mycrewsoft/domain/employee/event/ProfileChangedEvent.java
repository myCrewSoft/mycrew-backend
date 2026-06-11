package com.mycrewsoft.domain.employee.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 개인정보 변경 알림
@Getter
@RequiredArgsConstructor
public class ProfileChangedEvent {
    private final Long empId;    
}
