package com.mycrewsoft.domain.schedule.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntgSchdVO {

	private Long SchdId;		// 일정 ID
	
	private Long SchdClsfCd;	// 일정 구분 코드
	
	private Long DeptCd;		// 부서 코드
	private Long ProjId;		// 프로젝트 코드
	
	private Long SchdNm;		// 일정명
	private Long SchdDetailCn;	// 일정 상세 내용
	private Long BeginDt;		// 시작 일시
	private Long EndDt;			// 종료 일시
	
	private Long SchdWrtrId;	// 일정 작성자 ID
	private Long SchdRegstDt;	// 일정 등록 일시
	
	private Long SchdChgrId;	// 일정 수정자 ID
	private Long SchdChgDt;		// 일정 변경 일시
	
	private Long DelYn;			// 삭제 여부
	private Long DelDt;			// 삭제 일시
	
}
