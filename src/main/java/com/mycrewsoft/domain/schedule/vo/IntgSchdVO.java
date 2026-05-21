package com.mycrewsoft.domain.schedule.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntgSchdVO {

	private Long schdId;		// 일정 ID
	
	private String schdClsfCd;	// 일정 구분 코드
	
	private String deptCd;		// 부서 코드
	private String projId;		// 프로젝트 코드
	
	private String schdNm;		// 일정명
	private String schdDetailCn;	// 일정 상세 내용
	private LocalDateTime beginDt;		// 시작 일시
	private LocalDateTime endDt;			// 종료 일시
	
	private Long SchdWrtrId;	// 일정 작성자 ID
	private LocalDateTime SchdRegstDt;	// 일정 등록 일시
	
	private Long SchdChgrId;	// 일정 수정자 ID
	private LocalDateTime SchdChgDt;		// 일정 변경 일시
	
	private String DelYn;			// 삭제 여부
	private LocalDateTime DelDt;			// 삭제 일시
	
}
