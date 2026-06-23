package com.mycrewsoft.domain.search.factory;

import com.mycrewsoft.domain.search.dto.response.SearchResponse;
import com.mycrewsoft.domain.search.enums.SearchType;
import com.mycrewsoft.domain.search.vo.SearchVO;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** 검색 유형에 맞는 팩토리를 찾아주는 레지스트리입니다. */
@Component
public class SearchResultFactoryProvider {
    private final Map<SearchType, SearchResultFactory> factories;

    public SearchResultFactoryProvider(List<SearchResultFactory> factoryList) {
        this.factories = new EnumMap<>(SearchType.class);
        factoryList.forEach(factory -> factories.put(factory.getSupportedType(), factory));
    }

    public SearchResponse create(SearchVO vo) {
        SearchType type = SearchType.valueOf(vo.getType());
        SearchResultFactory factory = factories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("지원하지 않는 검색 유형입니다: " + type);
        }
        return factory.create(vo);
    }
}
