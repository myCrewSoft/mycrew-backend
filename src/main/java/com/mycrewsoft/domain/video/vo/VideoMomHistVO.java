package com.mycrewsoft.domain.video.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoMomHistVO {

	private Long histId;			// 수정 이력 Id - PK
	
	private Long momId;				// 회의록 Id - FK
	
	private String momCn;			// 회의록 내용
	
	private Long edtrId;			// 수정자 ID(TB_MEMBER)
	private LocalDateTime editDt;	// 수정 일시
}
