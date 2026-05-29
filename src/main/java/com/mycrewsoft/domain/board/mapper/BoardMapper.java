package com.mycrewsoft.domain.board.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;

import com.mycrewsoft.domain.board.dto.request.BoardSearchRequest;
import com.mycrewsoft.domain.board.vo.BoardVO;

/**
 * SQL 쿼리를 찾아서 DB에 대신 실행해주는 것
 */
@Mapper
public interface BoardMapper {
    
    /**
     * 💡 하나로 합친 통합 게시글 목록 조회 (동적 검색 및 유형별 필터링)
     * @param searchRequest 검색어, 게시판유형코드, 부서코드가 담긴 요청 DTO
     * @return 필터링된 게시글 목록
     */
    List<BoardVO> selectBoardList(
    	BoardSearchRequest condition,
    	Long currentEmpId, 
    	String myDeptCd,
    	boolean global,
        Set<String>  deptScopeIds,
        Set<String>  projectScopeIds
        
    );
    
    
    
}