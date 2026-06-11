package com.mycrewsoft.domain.employee.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 권한 변경 알림
@Getter
@RequiredArgsConstructor
public class PermissionChangedEvent {
    private final Long empId;    
}
