package com.mycrewsoft.domain.attendance.service;

/**
 * 근태 도메인 상수. 근태 상태 코드(TB_ATND_SE_CD)와 조회 기간 구분을 정의한다.
 */
public final class AttendanceConstants {

	private AttendanceConstants() {}

	// 근태 상태 코드 (TB_ATND_SE_CD.ATND_SE_CD 시드값과 일치해야 함)
	public static final String STAT_WORKING = "WORKING";	// 근무중
	public static final String STAT_NORMAL  = "NORMAL";		// 정상
	public static final String STAT_LATE    = "LATE";		// 지각
	public static final String STAT_EARLY   = "EARLY";		// 조퇴
	public static final String STAT_ABSENT  = "ABSENT";		// 결근
	public static final String STAT_LEAVE   = "LEAVE";		// 휴가(종일)
	public static final String STAT_HALF    = "HALF";		// 반차
	public static final String STAT_HOLIDAY = "HOLIDAY";	// 휴일

	// 조회 기간 구분
	public static final String PERIOD_DAY   = "DAY";
	public static final String PERIOD_WEEK  = "WEEK";
	public static final String PERIOD_MONTH = "MONTH";
	public static final String PERIOD_YEAR  = "YEAR";
}
