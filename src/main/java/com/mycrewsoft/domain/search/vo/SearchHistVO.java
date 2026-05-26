package com.mycrewsoft.domain.search.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchHistVO {

	private Long searchHistId;		// 검색 이력 ID - PK
	
	private Long mbrId;				// 검색자 ID(TB_MEMBER)
	private String keyword;			// 검색어
	private LocalDateTime regDt;	// 검색 일시
}
