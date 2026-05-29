package com.mycrewsoft.domain.schedule.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchdTargetVO {

	private Long schdId;	// 일정 ID
	
	private String targetTypeCd; // 대상 타입 코드
	
	private String targetId;	// 대상 ID
}
