package com.mycrewsoft.domain.board.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardCommentVO {

	private Long commentId; //댓글 id
	private Long boardId; //게시판 id
	private String commentCn; //댓글내용
	private Integer wrterEmpId; //작성자 id
	private LocalDateTime wrteDt; //작성일시	
	private Integer commentPrtId;//댓글 부모 id
	private Integer commentDepth; //댓글 깊이
	private Integer commentOrder; //댓글순서
}
