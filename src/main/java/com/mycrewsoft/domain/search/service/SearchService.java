package com.mycrewsoft.domain.search.service;

import com.mycrewsoft.domain.search.dto.response.SearchResponse;
import com.mycrewsoft.domain.search.enums.SearchType;
import com.mycrewsoft.domain.search.vo.RecentItemVO;
import com.mycrewsoft.domain.search.vo.SearchHistVO;

import java.util.List;

public interface SearchService {

    List<SearchResponse> search(String keyword, SearchType type);

    List<SearchHistVO> readSearchHist();

    void deleteSearchHist(Long searchHistId);

    void deleteAllSearchHist();

    void mergeRecentItem(SearchType itemType, Long itemId);

    List<RecentItemVO> readRecentItems();

    void deleteRecentItem(Long recentItemId);
}