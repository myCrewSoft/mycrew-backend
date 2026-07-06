package com.mycrewsoft.domain.attendance.service;

import java.util.List;

import com.mycrewsoft.domain.attendance.dto.request.AtndPolicySaveRequest;
import com.mycrewsoft.domain.attendance.dto.response.AdminAtndRowResponse;
import com.mycrewsoft.domain.attendance.dto.response.AdminAtndStatResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndCheckResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndHistoryResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndPolicyResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndStatsResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndTodayResponse;

/**
 * 근태 관리 서비스. 사원의 출퇴근/통계와 관리자의 정책 설정/전체 조회를 담당한다.
 */
public interface AttendanceService {

	// ===== 사원 =====

	/** 출근 처리(정책 기반 지각 판정) */
	AtndCheckResponse checkIn(String clientIp);

	/** 퇴근 처리(정책 기반 정상/연장/초과 판정) */
	AtndCheckResponse checkOut(String clientIp);

	/** 오늘 본인 근태 현황 조회 */
	AtndTodayResponse getMyToday();

	/** 기간(DAY/WEEK/MONTH/YEAR)별 본인 통계 지표 조회 */
	AtndStatsResponse getMyStats(String period, String baseDate);

	/** 본인 근태 이력 조회(최근 N건) */
	List<AtndHistoryResponse> getMyHistory(int days);

	/** 지정한 월(YYYY-MM)의 본인 일자별 근태 조회 */
	List<AtndHistoryResponse> getMyMonth(String ym);

	// ===== 관리자 =====

	/** 현재 유효한 근무 정책 조회(없으면 null) */
	AtndPolicyResponse getActivePolicy();

	/** 근무 정책 저장(기존 버전 종료 + 신규 버전 생성) */
	AtndPolicyResponse savePolicy(AtndPolicySaveRequest request);

	/** 전체 사원 근태 현황 조회(특정 일자) */
	List<AdminAtndRowResponse> getAllAttendance(String date, String deptCd, String keyword);

	/** 기간별 사원 근태 집계 조회 */
	List<AdminAtndStatResponse> getAttendanceStats(String from, String to, String deptCd, String keyword);

	/** 특정 사원의 기간 일자별 근태 조회 */
	List<AdminAtndRowResponse> getEmployeeAttendance(Long empId, String from, String to);

	// ===== 대시보드 위젯 =====

	/** 오늘 근태 특이사항(지각/조퇴/결근) 사원 목록 조회(최대 limit명, 관리자 위젯용) */
	List<AdminAtndRowResponse> getAttendanceAnomaliesForWidget(int limit);
}
