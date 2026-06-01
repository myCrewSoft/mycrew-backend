package com.mycrewsoft.domain.video.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoChatLogVO {

	private Long chatLogId;			// 녹취록 ID - PK
	private Long vconfId;			// 화상방 ID - FK

	private Long mbrId;				// 발언자ID(TB_MEMBER)
	private Long spkngCn;			// 발언 내용
	
	private LocalDateTime creatDt;	// 생성 일시
	private LocalDateTime endDt;	// 종료 일시
}
