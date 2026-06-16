package com.mycrewsoft.domain.dashboard.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 사용자 대시보드 레이아웃 VO
 */
@Getter
@Setter
public class DashboardLayoutVO {
	
	private Long dshbdLytId;	// 대시보드ID - PK
	
    private Long empId;			// 사원ID - TB_EMPLOYEE 논리 참조
    private String lytJsonCn;		// 레이아웃 내용
    private LocalDateTime creatDt;		// 생성일시
    private LocalDateTime lastMdfcnDt;	// 최종수정일시
}
