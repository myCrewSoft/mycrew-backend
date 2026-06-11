package com.mycrewsoft.domain.board.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 게시글 삭제 알림
@Getter
@RequiredArgsConstructor
public class PostDeletedByAdminEvent {
    private final String postTitle;
    private final Long rcvrEmpId;    
}
