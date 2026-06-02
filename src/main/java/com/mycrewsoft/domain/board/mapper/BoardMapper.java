package com.mycrewsoft.domain.board.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // 💡 반드시 org.apache.ibatis.annotations.Param 이어야 합니다!

import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.dto.response.BoardResponse;
import com.mycrewsoft.domain.board.dto.response.BoardSideBarResponse;

@Mapper
public interface BoardMapper {
    
    /**
     *  전체 카운트 및  리스트 
     * @param searchRequest
     * @return
     */
    long countBoard(BoardSearchRequest searchRequest,String boardTypeCd,String deptCd);
	
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
	 *   사이드바 목록 조회
	 */
	List<BoardSideBarResponse> getSideBar(
		@Param("myDeptCd")	String myDeptCd,
		@Param("empId") Long currentEmpId,
		@Param("hasGlobal")	boolean hasGlobal, 
		@Param("departmentScopeIds") Set<String> departmentScopeIds
	);
}