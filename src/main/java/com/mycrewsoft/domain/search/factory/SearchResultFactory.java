package com.mycrewsoft.domain.search.factory;

import com.mycrewsoft.domain.search.dto.response.SearchResponse;
import com.mycrewsoft.domain.search.dto.response.SearchResultDetails;
import com.mycrewsoft.domain.search.enums.SearchType;
import com.mycrewsoft.domain.search.vo.SearchVO;

/** 공통 생성 순서는 고정하고 유형별 details와 URL 생성은 하위 클래스에 위임합니다. */
public abstract class SearchResultFactory {

    public final SearchResponse create(SearchVO vo) {
        return SearchResponse.builder()
                .id(vo.getId())
                .parentId(vo.getParentId())
                .type(getSupportedType())
                .title(vo.getTitle())
                .summary(vo.getSummary())
                .url(createUrl(vo))
                .details(createDetails(vo))
                .build();
    }

    public abstract SearchType getSupportedType();
    protected abstract String createUrl(SearchVO vo);
    protected abstract SearchResultDetails createDetails(SearchVO vo);

    protected String firstNonNull(String primary, String fallback) {
        return primary != null ? primary : fallback;
    }
}
