package com.mycrewsoft.domain.search.dto.response;

import com.mycrewsoft.domain.search.enums.SearchType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchResponse {

    private Long id;

    private Long parentId;

    private SearchType type;

    private String title;

    private String description;

    private String badgeText;
}