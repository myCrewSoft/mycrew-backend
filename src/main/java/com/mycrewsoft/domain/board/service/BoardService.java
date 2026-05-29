package com.mycrewsoft.domain.board.service;

import java.util.List;
import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.vo.BoardVO;

public interface BoardService {
    
    /**
     * 💡 하나로 합친 통합 게시글 목록 조회 인터페이스
     * @param searchRequest 검색어, 게시판유형코드, 부서코드가 담긴 요청 DTO
     * @return 필터링된 게시글 목록
     */

    
    List<BoardVO> selectBoardList(BoardSearchRequest searchRequest);


}