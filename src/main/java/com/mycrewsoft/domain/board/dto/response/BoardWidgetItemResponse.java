package com.mycrewsoft.domain.board.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "게시판 위젯 항목 응답")
public class BoardWidgetItemResponse {

    @Schema(description = "게시글 ID")
    private Long id;

    @Schema(description = "게시글 제목")
    private String title;

    @Schema(description = "작성자 이름")
    private String writerName;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;
}