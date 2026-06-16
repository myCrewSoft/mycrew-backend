package com.mycrewsoft.domain.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "댓글 생성 Request DTO")
public class BoardCommentCreateRequest {

    @NotNull(message = "게시판 ID는 필수입니다.")
    @Schema(description = "게시판 ID", example = "84")
    private Long boardId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 1000, message = "댓글은 1000자 이하여야 합니다.")
    @Schema(description = "댓글 내용", example = "좋은 정보 감사합니다!")
    private String commentCn;

    @Schema(description = "댓글 부모 ID (대댓글일 경우 부모의 commentId 입력 / 일반 댓글은 null)", example = "12")
    private Integer commentPrtId;

    @NotNull(message = "댓글 깊이는 필수입니다.")
    @Schema(description = "댓글 깊이 (일반댓글: 0 / 대댓글: 1)", example = "0", defaultValue = "0")
    private Integer commentDepth = 0;

    @NotNull(message = "댓글 순서는 필수입니다.")
    @Schema(description = "같은 그룹 내 댓글 순서", example = "1", defaultValue = "1")
    private Integer commentOrder = 1;
}