package com.mycrewsoft.domain.dashboard.dto.response.widget;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 대시보드 - 공지사항 위젯 응답 (최신 공지 목록)")
public class AdminNoticeWidgetResponse {

    @Schema(description = "공지사항 목록 (최신 5개)")
    private List<NoticeItem> notices;

    @Getter
    @Builder
    @Schema(description = "공지사항 항목")
    public static class NoticeItem {

        @Schema(description = "게시글 ID", example = "320")
        private Long id;

        @Schema(description = "공지 제목", example = "사내 보안 정책 개정 안내")
        private String title;

        @Schema(description = "작성자 이름", example = "관리자")
        private String writerName;

        @Schema(description = "작성일시 (yyyy-MM-dd HH:mm)", example = "2026-06-17 14:05")
        private String createdAt;
    }
}
