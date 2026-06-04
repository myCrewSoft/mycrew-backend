package com.mycrewsoft.domain.board.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardVO {

	private Long boardId; //게시판 아이디
	private String boardTypeCd; //게시판 유형코드
	private String boardSj; // 게시판 제목
	private String boardCn; //게시판 내용
	private Integer frstRgtrId; //최초 등록자 아이디
	private LocalDateTime frstRegDt; //최초 등록 일시
	private LocalDateTime lastMdfrDt; //최종 수정 일시
	private Integer boardAtchFileId; // 게시판 첨부 파일 아이디
	private String deptCd; // 부서코드
	private Integer projId; //프로젝트 아이디
	private String imprtntYn; //중요여부
	private String cmntUseYn; //댓글 허용
	private Integer viewCnt; // 조회수
	
	private List<BoardCommentVO>commentList; // 하나의 게시글 상세 내용을 불러올 때 댓글 목록도 함께
	private List<BoardAnlsVO> boardAnlsList; // 하나의 게시글 여러 분석 결과 목록을 가짐
}
