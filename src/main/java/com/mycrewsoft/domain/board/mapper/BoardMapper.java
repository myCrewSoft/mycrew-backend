package com.mycrewsoft.domain.board.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // 💡 반드시 org.apache.ibatis.annotations.Param 이어야 합니다!

import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.dto.response.BoardResponse;
import com.mycrewsoft.domain.board.dto.response.BoardSideBarResponse;
import com.mycrewsoft.domain.board.vo.BoardCommentVO;
import com.mycrewsoft.domain.board.vo.BoardLikeVo;
import com.mycrewsoft.domain.board.vo.BoardVO;
import com.mycrewsoft.domain.board.vo.BoardWidgetVO;


@Mapper
public interface BoardMapper {

	/**
	 *  전체 카운트 및  리스트 
	 * @param searchRequest
	 * @return
	 */
	int countBoard(
		    @Param("searchRequest") BoardSearchRequest searchRequest, //검색어
		    @Param("boardTypeCd") String boardTypeCd, //게시판 유형
		    @Param("deptCd") String deptCd //부서유형
		);

	/**
	 * 권한 스코프와 페이징 처리가 결합된 통합 게시글 목록 조회
	 */
	List<BoardResponse> getBoardList(
			@Param("getOffset") long getOffset,
			@Param("getPageSize") int getPageSize,
			@Param("searchRequest") BoardSearchRequest searchRequest,
			@Param("boardTypeCd")String boardTypeCd,
			@Param("deptCd")String deptCd
			);
	
	
	
	/**
	 *  내 게시글 전체 카운트 (작성자 기준)
	 */
	int countMyBoard(
			@Param("empId") Long empId,
			@Param("searchRequest") BoardSearchRequest searchRequest
		);

	/**
	 *  내 게시글 목록 조회 (작성자 기준, 페이징)
	 */
	List<BoardResponse> getMyBoardList(
			@Param("getOffset") long getOffset,
			@Param("getPageSize") int getPageSize,
			@Param("empId") Long empId,
			@Param("searchRequest") BoardSearchRequest searchRequest
		);

	/**
	 *   프로젝트 목록 조회
	 */

	List<BoardResponse>	getProjList(
			@Param("getOffset") long getOffset, //페이징 시작위치
			@Param("getPageSize") int getPageSize, //한 페이지당 가져올 게시글 수
			@Param("searchRequest") BoardSearchRequest searchRequest,
			@Param("projId") Long projId

			);
	
	int countProjBoard(
			@Param("searchRequest")BoardSearchRequest searchRequest,
			@Param("projId") Long projId
			
			);
	

	/**
	 *   사이드바 목록 조회
	 */
	List<BoardSideBarResponse> getSideBar(
			@Param("myDeptCd")	String myDeptCd,
			@Param("empId") Long currentEmpId,
			@Param("hasGlobal")	boolean hasGlobal, 
			@Param("departmentScopeIds") Set<String> departmentScopeIds
			);

	// 게시판 조회 
	BoardVO readBoard(@Param("boardId") Long boardId);
	
	//게시판 댓글 조회
	List<BoardCommentVO>  readCommentList(@Param("boardId") Long boardId);
	
	//게시판 댓글 작성자 아이디 조회
	Long readCmWrterEmpId(@Param("commentId") Long commentId);
	
	

	//게시판 조회수 증가
	 int updateViewCount(@Param("boardId") Long boardId);
	 
	
	//게시판 좋아요 읽기
	BoardLikeVo readLikeStatus(
			@Param("boardId") Long boardId,
			@Param("empId")  Long empId);
	
	 // 좋아요 수 
	 int  readLikeCount(@Param("boardId") Long boardId);
	 
	 //게시글 좋아요 등록
	 void insertLike(BoardLikeVo likeVo);
	 
	 //게시글 좋아요 취소
	 int deleteLike(@Param("boardId")Long boardId,
			 @Param("empId")Long empId);
	 
	 
	 // 게시글 생성
	 void createBoard(BoardVO boardVo);
	 
	 // 게시글 수정
	  int updateBoardDetails(BoardVO updateBoardDetails);
	 
	  //게시글 삭제
	  int deleteBoardDetail(Long boardId);
	  
	  //게시글 댓글 생성
	  void insertComment(BoardCommentVO commentVo);
	  
	  //게시글 댓글 수정
	  int updateComment(BoardCommentVO updateComment);
	  
	  //게시글 댓글 삭제
	  int deleteComment(@Param("commentId") Long commentId);
	  
	  //위젯용
	  List<BoardWidgetVO> getBoardListForWidget(
		  @Param("boardTypeCd") String boardTypeCd,
          @Param("deptCd") String deptCd,
          @Param("limit") int limit
      );
	  
}