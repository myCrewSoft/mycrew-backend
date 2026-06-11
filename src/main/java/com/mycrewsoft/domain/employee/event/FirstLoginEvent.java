package com.mycrewsoft.domain.employee.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 첫 로그인시 알림
@Getter
@RequiredArgsConstructor
public class FirstLoginEvent {
    private final Long empId;
}
