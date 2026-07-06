package com.mycrewsoft.domain.search.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecentItemVO {

	private Long recentItemId;			// 최근 항목 ID - PK
	
	private Long empId;					// 사용자 ID(TB_EMPLOYEE)
	private String itemType;			// 항목 타입(01: 게시글 / 02 프로젝트 등)
	private Long itemId;				// 항목 ID
	private LocalDateTime accessedDt;	// 마지막 접근 일시
}
