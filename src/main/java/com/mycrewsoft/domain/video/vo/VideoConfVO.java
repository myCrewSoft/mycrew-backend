package com.mycrewsoft.domain.video.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoConfVO {

	private Long vconfId;			// 화상방 ID - PK
	
	private String vconfNm;			// 화상방 이름
	private String roomNm;
	private Long crtrId;			// 생성자 ID(TB_MEMBER)
	private String confSttusCd;		// 회의 상태 (01: 대기 / 02: 진행 중 / 03: 종료)
	
	private LocalDateTime beginDt;	// 시작 일시
	private LocalDateTime endDt;	// 종료 일시
	private LocalDateTime creatDt;	// 생성 일시
	
	// has many
	private List<VideoPtcptVO> videoPtcpt;	// 참여자 목록
	private List<VideoRcrdgVO> videoRcrdg;	// 녹취록 목록
	private List<VideoChatLogVO> videoChatLog;
	
	// has a
	private VideoMomVO videoMom; // 회의록
}
