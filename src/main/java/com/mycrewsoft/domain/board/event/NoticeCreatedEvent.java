package com.mycrewsoft.domain.board.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 공지사항 등록 알림
@Getter
@RequiredArgsConstructor
public class NoticeCreatedEvent {
    private final String postTitle;
    private final List<Long> allEmpIds;  
}
