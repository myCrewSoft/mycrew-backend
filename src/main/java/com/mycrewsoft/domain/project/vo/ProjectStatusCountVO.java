package com.mycrewsoft.domain.project.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 전체 프로젝트 상태별 집계 결과 VO. 관리자 대시보드 프로젝트 현황 위젯에서 사용한다.
 * 상태 코드: 01(예정) / 02(진행 중) / 03(완료) / 04(중단)
 * 예정 상태이지만 시작일이 도래한 프로젝트는 진행 중으로 집계한다.
 */
@Getter
@Setter
public class ProjectStatusCountVO {

	private int totalCount;			// 전체 프로젝트 수
	private int plannedCount;		// 예정(01)
	private int inProgressCount;	// 진행 중(02)
	private int completedCount;		// 완료(03)
	private int stoppedCount;		// 중단(04)
}
