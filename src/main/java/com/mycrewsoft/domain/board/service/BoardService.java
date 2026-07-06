package com.mycrewsoft.domain.board.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mycrewsoft.domain.board.dto.request.BoardCommentCreateRequest;
import com.mycrewsoft.domain.board.dto.request.BoardCommentUpdateRequest;
import com.mycrewsoft.domain.board.dto.request.BoardCreateRequest;
import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.dto.request.BoardUpdateRequest;
import com.mycrewsoft.domain.board.dto.response.BoardResponse;
import com.mycrewsoft.domain.board.dto.response.BoardSideBarResponse;
import com.mycrewsoft.domain.board.dto.response.BoardWidgetItemResponse;

public interface BoardService {


	/**
	 *  게시글 목록 조회
	 * @param boardTypeCd
	 * @param deptCd
	 * @param searchRequest
	 * @param pageable
	 * @return
	 */
	Page<BoardResponse> getBoardList(String boardTypeCd, String deptCd, BoardSearchRequest searchRequest,Pageable pageable);

	/**
	 * 내 게시글 목록 조회 (현재 로그인 사용자가 작성한 게시글)
	 * @param searchRequest 검색 조건(키워드)
	 * @param pageable 페이징 정보
	 * @return 작성자 기준 게시글 페이지
	 */
	Page<BoardResponse> getMyBoardList(BoardSearchRequest searchRequest, Pageable pageable);

	/**
	 *  SideBar 목록을 가져오는 메서드. 
	 * @return
	 */
	List<BoardSideBarResponse> getSideBar();

	
	// 게시판 게시글 읽기
	BoardResponse getBoard(String deptCd,Long boardId);
	
	// 프로젝트 목록 조회
	Page<BoardResponse> getProjList(Long projId, BoardSearchRequest searchRequest,Pageable pageable);
	
	
	// 게시글 생성 
	Long createBoard(BoardCreateRequest boardCreateRequest);
	
	// 게시글 수정
	Long updateBoardDetail(Long boardId,BoardUpdateRequest boardUpdateRequest);
	
	//게시글 삭제
	void deleteBoardDetail(Long boardId);
	
	
	// 게시글 댓글 생성
	Long createComment(BoardCommentCreateRequest createComment);
	
	
	//게시글 댓글 수정
	
	Long updateComment(BoardCommentUpdateRequest updateComment);
	
	//게시글 댓글 삭제
	void deleteComment(Long commentId);
	
	
	//게시글 좋아요 토글 (등록 취소)
	boolean toggelLike(Long  boardId,Long empId);
	
	//게시글 상세 좋아요 정보(총개수 +본인 좋아요 여부)
	
	Map<String, Object> getLike(Long boardId,Long empId);
	
	List<BoardWidgetItemResponse> getBoardListForWidget(String boardTypeCd, int limit);

}