package com.mycrewsoft.domain.schedule.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IntgSchdVO {

	private Long schdId;		// 일정 ID
	
	private String schdClsfCd;	// 일정 구분 코드
	
	private String schdNm;		// 일정명
	private String schdDetailCn;	// 일정 상세 내용
	private LocalDateTime beginDt;		// 시작 일시
	private LocalDateTime endDt;			// 종료 일시
	
	private Long schdWrtrId;	// 일정 작성자 ID
	private LocalDateTime schdRegstDt;	// 일정 등록 일시
	
	private Long schdChgrId;	// 일정 수정자 ID
	private LocalDateTime schdChgDt;		// 일정 변경 일시
	
	private String allDayYn;	// 종일 여부
	
	private String delYn;			// 삭제 여부
	private LocalDateTime delDt;			// 삭제 일시
	
	private String reptYn;			// 반복 여부
	private String reptTypeCd;		// 반복 타입
	private String reptEndDt;		// 반복 종료일
	
	private List<SchdTargetVO> targets;	// 일정 해당자
	
}
