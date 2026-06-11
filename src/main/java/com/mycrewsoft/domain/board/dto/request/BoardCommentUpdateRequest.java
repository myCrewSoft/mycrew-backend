package com.mycrewsoft.domain.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "댓글 수정 Request DTO")
public class BoardCommentUpdateRequest {

    @NotNull(message = "댓글 ID는 필수입니다.")
    @Schema(description = "수정할 댓글 ID", example = "5")
    private Long commentId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Schema(description = "수정할 댓글 내용", example = "수정된 내용입니다.")
    private String commentCn;
}