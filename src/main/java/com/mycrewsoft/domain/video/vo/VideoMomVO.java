package com.mycrewsoft.domain.video.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoMomVO {

	private Long momId;				// 회의록 ID - PK
	
	private Long vconfId;			// 화상방 ID(TB_VIDEO_CONF)

	private String momCn;				// 회의록 내용
	
	private LocalDateTime creatDt;	// 생성 일시
	private LocalDateTime delDt;	// 삭제 일시

	private String momSttusCd;	// 회의록 상태
	private Long edtrId;		// 담당자 ID (TB_MEMBER)
	
	private LocalDateTime revwReqDt;	// 검토 요청 발송 일시
	private LocalDateTime cnfrmDt;		// 정식 등록 일시
	
	private Integer aprvlRoundNo;		// 결제 회차
	
	// has many
	private List<VideoMomAprvlVO> videoMomAprvl;	// 회의록 결재자 목록
	private List<VideoMomHistVO> videoMomHist;		// 회의록 수정 이력 목록
	
}
