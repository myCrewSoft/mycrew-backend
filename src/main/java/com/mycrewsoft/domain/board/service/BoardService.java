package com.mycrewsoft.domain.board.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.dto.response.BoardResponse;

public interface BoardService {
    
    /**
     * 💡 하나로 합친 통합 게시글 목록 조회 인터페이스
     * @param searchRequest 검색어, 게시판유형코드, 부서코드가 담긴 요청 DTO
     * @return 필터링된 게시글 목록
     */

    
 

    
    /**
	 * 관리자가 게시글 목록을 검색하는 메서드. 검색 조건에 따라 게시글 목록을 페이지 형태로 반환한다.
	 * @param BoardResponse condition
	 * @return Page<BoardResponse>
	 */
    
    
    
   Page<BoardResponse> getBoard(BoardSearchRequest condition);

}