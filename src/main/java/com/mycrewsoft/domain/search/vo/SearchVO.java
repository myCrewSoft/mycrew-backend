package com.mycrewsoft.domain.search.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchVO {

    private Long id;

    private Long parentId;

    private String type;

    private String title;

    private String description;

    private String badgeText;
}