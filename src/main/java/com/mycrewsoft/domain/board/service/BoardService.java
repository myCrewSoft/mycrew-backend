package com.mycrewsoft.domain.board.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.dto.response.BoardResponse;
import com.mycrewsoft.domain.board.dto.response.BoardSideBarResponse;

public interface BoardService {

	/**
	 * 관리자가 게시글 목록을 검색하는 메서드. 검색 조건에 따라 게시글 목록을 페이지 형태로 반환한다.
	 * 
	 * @param BoardResponse condition
	 * @return Page<BoardResponse>
	 */
	Page<BoardResponse> getBoard(String boardTypeCd, String deptCd, BoardSearchRequest searchRequest,Pageable pageable);

	/**
	 *  SideBar 목록을 가져오는 메서드. 
	 * @return
	 */
	List<BoardSideBarResponse> getSideBar();

}