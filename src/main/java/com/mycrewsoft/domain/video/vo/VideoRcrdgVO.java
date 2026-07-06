package com.mycrewsoft.domain.video.vo;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VideoRcrdgVO {

	private Long rcrdgId;			// 녹취록 ID - PK
	private Long vconfId;			// 화상방 ID - FK

	private Long rcrdgAtchFileId;	// 녹취록 ID - FK
	
	private LocalDateTime creatDt;	// 생성 일시
	private LocalDateTime delDt;	// 삭제 일시
}
