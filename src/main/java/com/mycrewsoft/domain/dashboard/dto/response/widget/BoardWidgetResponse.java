package com.mycrewsoft.domain.dashboard.dto.response.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "게시판 위젯 응답")
public class BoardWidgetResponse {

    @Schema(description = "게시글 목록")
    private List<BoardItem> posts;

    @Getter
    @Builder
    public static class BoardItem {

        @Schema(description = "게시글 ID")
        private Long id;

        @Schema(description = "게시글 제목")
        private String title;

        @Schema(description = "작성자 이름")
        private String writerName;

        @Schema(description = "작성일시")
        private String createdAt;

        @Schema(description = "새 글 여부")
        private boolean isNew;
    }
}