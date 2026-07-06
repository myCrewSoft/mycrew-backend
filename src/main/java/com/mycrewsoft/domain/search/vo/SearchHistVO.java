package com.mycrewsoft.domain.search.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchHistVO {

	private Long searchHistId;		// 검색 이력 ID - PK
	
	private Long empId;				// 검색자 ID(TB_EMPLOYEE)
	private String keyword;			// 검색어
	private LocalDateTime regDt;	// 검색 일시
}
