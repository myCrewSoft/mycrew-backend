package com.mycrewsoft.domain.board.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 댓글 등록 알림
@Getter
@RequiredArgsConstructor
public class CommentCreatedEvent {
    private final String postTitle;
    private final Long rcvrEmpId;      
}
