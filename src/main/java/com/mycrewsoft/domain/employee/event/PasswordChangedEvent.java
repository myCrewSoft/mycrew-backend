package com.mycrewsoft.domain.employee.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 비밀번호 변경 알림
@Getter
@RequiredArgsConstructor
public class PasswordChangedEvent {
    private final Long empId;
}
