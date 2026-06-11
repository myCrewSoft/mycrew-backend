package com.mycrewsoft.domain.employee.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 권한 변경 알림
@Getter
@RequiredArgsConstructor
public class PermissionChangedEvent {
    private final List<Long> empIds;    
}
