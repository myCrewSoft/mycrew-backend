package com.mycrewsoft.domain.search.mapper;

import com.mycrewsoft.domain.search.vo.SearchHistVO;
import com.mycrewsoft.domain.search.vo.SearchVO;
import com.mycrewsoft.domain.search.vo.RecentItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SearchMapper {

	// 키워드 + 타입으로 검색
	List<SearchVO> search(
		@Param("keyword") String keyword,
        @Param("empId") Long empId
    );

    // 검색 이력 저장
    void insertSearchHist(SearchHistVO vo);

    // 내 검색 이력 조회
    List<SearchHistVO> readSearchHist(Long mbrId);

    // 검색 이력 단건 삭제
    void deleteSearchHist(Long searchHistId);

    // 검색 이력 전체 삭제
    void deleteAllSearchHist(Long mbrId);

    // 최근 항목 저장
    void mergeRecentItem(RecentItemVO vo);

    // 최근 본 항목 목록 조회
    List<RecentItemVO> readRecentItems(Long mbrId);

    // 최근 항목 단건 삭제
    void deleteRecentItem(Long recentItemId);
}