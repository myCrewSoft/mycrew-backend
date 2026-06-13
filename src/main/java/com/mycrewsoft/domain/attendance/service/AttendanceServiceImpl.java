package com.mycrewsoft.domain.attendance.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.attendance.dto.request.AtndPolicySaveRequest;
import com.mycrewsoft.domain.attendance.dto.response.AdminAtndRowResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndCheckResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndHistoryResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndPolicyResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndStatsResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndTodayResponse;
import com.mycrewsoft.domain.attendance.mapper.AttendanceMapper;
import com.mycrewsoft.domain.attendance.vo.AtndDailyVO;
import com.mycrewsoft.domain.attendance.vo.AtndPolicyVO;
import com.mycrewsoft.domain.attendance.vo.AtndStatsAggVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

	private final AttendanceMapper attendanceMapper;
	private final AuthorizationService authorizationService;

	private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	// ============================ 사원 ============================

	@Override
	@Transactional
	public AtndCheckResponse checkIn(String clientIp) {
		Long empId = SecurityUtil.getCurrentEmpId();
		LocalDate today = LocalDate.now();

		AtndDailyVO existing = attendanceMapper.selectDaily(empId, today);
		if (existing != null) {
			throw new CustomException(ErrorCode.ATND_ALREADY_CHECKED_IN);
		}

		AtndPolicyVO policy = requireActivePolicy(today);
		LocalDateTime now = LocalDateTime.now();

		LocalDateTime lateThreshold = today.atTime(parseTime(policy.getWorkBgnTm()))
				.plusMinutes(nz(policy.getLateGraceMin()));
		int lateMin = minutesAfter(lateThreshold, now);
		String status = lateMin > 0 ? AttendanceConstants.STAT_LATE : AttendanceConstants.STAT_WORKING;

		AtndDailyVO daily = new AtndDailyVO();
		daily.setEmpId(empId);
		daily.setAtndDt(today);
		daily.setAtndStatCd(status);
		daily.setApplyPolicyId(policy.getAtndPolicyId());
		daily.setWrkStartDtm(now);
		daily.setChkinIp(clientIp);
		daily.setLateMin(lateMin);
		attendanceMapper.insertDaily(daily);

		return AtndCheckResponse.builder()
				.atndDt(today)
				.atndStatCd(status)
				.atndStatNm(statNm(status))
				.wrkStartDtm(now)
				.lateMin(lateMin)
				.earlyLeaveMin(0)
				.workMin(0)
				.otMin(0)
				.message(lateMin > 0
						? "출근 처리되었습니다. (지각 " + lateMin + "분)"
						: "출근 처리되었습니다.")
				.build();
	}

	@Override
	@Transactional
	public AtndCheckResponse checkOut(String clientIp) {
		Long empId = SecurityUtil.getCurrentEmpId();
		LocalDate today = LocalDate.now();

		AtndDailyVO daily = attendanceMapper.selectDaily(empId, today);
		if (daily == null || daily.getWrkStartDtm() == null) {
			throw new CustomException(ErrorCode.ATND_NOT_CHECKED_IN);
		}
		if (daily.getWrkEndDtm() != null) {
			throw new CustomException(ErrorCode.ATND_ALREADY_CHECKED_OUT);
		}

		AtndPolicyVO policy = requireActivePolicy(today);
		LocalDateTime now = LocalDateTime.now();

		int grossMin = (int) Duration.between(daily.getWrkStartDtm(), now).toMinutes();
		int workMin = Math.max(0, grossMin - nz(policy.getBreakMin()));
		int stdDay = nz(policy.getStdWorkMinDay());
		int normalWorkMin = Math.min(workMin, stdDay);
		int otCandidate = Math.max(0, workMin - stdDay);
		// 사전 승인된 초과근무(전자결재 승인 완료분)를 승인근무로 인정
		int approvedToday = nz(attendanceMapper.selectApprovedOtMin(empId, today));
		int approvedOtMin = Math.min(otCandidate, approvedToday);
		int otMin = otCandidate;               // 연장근무 전체
		int excessMin = otCandidate - approvedOtMin;

		LocalDateTime workEnd = today.atTime(parseTime(policy.getWorkEndTm()));
		int earlyLeaveMin = minutesAfter(now, workEnd);

		int prevLate = nz(daily.getLateMin());
		String status;
		if (earlyLeaveMin > 0) {
			status = AttendanceConstants.STAT_EARLY;
		} else if (prevLate > 0) {
			status = AttendanceConstants.STAT_LATE;
		} else {
			status = AttendanceConstants.STAT_NORMAL;
		}

		daily.setAtndStatCd(status);
		daily.setWrkEndDtm(now);
		daily.setChkoutIp(clientIp);
		daily.setEarlyLeaveMin(earlyLeaveMin);
		daily.setWorkMin(workMin);
		daily.setNormalWorkMin(normalWorkMin);
		daily.setOtMin(otMin);
		daily.setApprovedOtMin(approvedOtMin);
		daily.setExcessMin(excessMin);
		attendanceMapper.updateDaily(daily);

		return AtndCheckResponse.builder()
				.atndDt(today)
				.atndStatCd(status)
				.atndStatNm(statNm(status))
				.wrkStartDtm(daily.getWrkStartDtm())
				.wrkEndDtm(now)
				.lateMin(prevLate)
				.earlyLeaveMin(earlyLeaveMin)
				.workMin(workMin)
				.otMin(otMin)
				.message("퇴근 처리되었습니다. (실근무 " + (workMin / 60) + "시간 " + (workMin % 60) + "분)")
				.build();
	}

	@Override
	@Transactional(readOnly = true)
	public AtndTodayResponse getMyToday() {
		Long empId = SecurityUtil.getCurrentEmpId();
		LocalDate today = LocalDate.now();
		AtndDailyVO daily = attendanceMapper.selectDaily(empId, today);

		if (daily == null) {
			return AtndTodayResponse.builder()
					.atndDt(today)
					.checkedIn(false)
					.checkedOut(false)
					.lateMin(0)
					.workMin(0)
					.build();
		}
		return AtndTodayResponse.builder()
				.atndDt(today)
				.checkedIn(daily.getWrkStartDtm() != null)
				.checkedOut(daily.getWrkEndDtm() != null)
				.atndStatCd(daily.getAtndStatCd())
				.atndStatNm(statNm(daily.getAtndStatCd()))
				.wrkStartDtm(daily.getWrkStartDtm())
				.wrkEndDtm(daily.getWrkEndDtm())
				.lateMin(nz(daily.getLateMin()))
				.workMin(nz(daily.getWorkMin()))
				.build();
	}

	@Override
	@Transactional(readOnly = true)
	public AtndStatsResponse getMyStats(String period) {
		Long empId = SecurityUtil.getCurrentEmpId();
		LocalDate today = LocalDate.now();
		String prd = period == null ? AttendanceConstants.PERIOD_WEEK : period.toUpperCase();

		LocalDate from;
		LocalDate to;
		String label;
		switch (prd) {
			case AttendanceConstants.PERIOD_DAY:
				from = today;
				to = today;
				label = today.format(YMD);
				break;
			case AttendanceConstants.PERIOD_WEEK:
				from = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
				to = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
				label = from.format(YMD) + " ~ " + to.format(YMD);
				break;
			case AttendanceConstants.PERIOD_MONTH:
				from = today.withDayOfMonth(1);
				to = today.with(TemporalAdjusters.lastDayOfMonth());
				label = today.getYear() + "-" + String.format("%02d", today.getMonthValue());
				break;
			case AttendanceConstants.PERIOD_YEAR:
				from = today.withDayOfYear(1);
				to = today.with(TemporalAdjusters.lastDayOfYear());
				label = String.valueOf(today.getYear());
				break;
			default:
				throw new CustomException(ErrorCode.ATND_INVALID_PERIOD);
		}

		AtndStatsAggVO agg = attendanceMapper.selectStatsAgg(empId, from, to);

		// 항상 '이번 주' 기준 잔여 지표
		LocalDate weekFrom = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate weekTo = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		AtndStatsAggVO weekAgg = attendanceMapper.selectStatsAgg(empId, weekFrom, weekTo);

		AtndPolicyVO policy = attendanceMapper.selectActivePolicy(today);
		int stdWorkMinWk = policy != null ? nz(policy.getStdWorkMinWk()) : 0;
		int maxOtMinWk = policy != null ? nz(policy.getMaxOtMinWk()) : 0;
		int remainingWorkMin = Math.max(0, stdWorkMinWk - nz(weekAgg.getNormalWorkMin()));
		int remainingOtMin = Math.max(0, maxOtMinWk - nz(weekAgg.getOtMin()));

		// 잔여 연차 = 정책 기본 연차 + 원장 합계(부여 - 사용)
		double annualDef = policy != null && policy.getAnnualLeaveDef() != null
				? policy.getAnnualLeaveDef()
				: 0d;
		Double ledgerSum = attendanceMapper.selectRemainAnnualLeave(empId, today.getYear());
		Double usedAnnual = attendanceMapper.selectUsedAnnualLeave(empId, today.getYear());
		double remainAnnual = annualDef + (ledgerSum == null ? 0d : ledgerSum);

		return AtndStatsResponse.builder()
				.period(prd)
				.periodLabel(label)
				.workMin(nz(agg.getWorkMin()))
				.normalWorkMin(nz(agg.getNormalWorkMin()))
				.otMin(nz(agg.getOtMin()))
				.approvedOtMin(nz(agg.getApprovedOtMin()))
				.excessMin(nz(agg.getExcessMin()))
				.lateCnt(nz(agg.getLateCnt()))
				.earlyLeaveCnt(nz(agg.getEarlyLeaveCnt()))
				.halfDayCnt(nz(agg.getHalfDayCnt()))
				.leaveUseDay(agg.getLeaveUseDay() == null ? 0d : agg.getLeaveUseDay())
				.remainingWorkMin(remainingWorkMin)
				.remainingOtMin(remainingOtMin)
				.stdWorkMinWk(stdWorkMinWk)
				.maxOtMinWk(maxOtMinWk)
				.remainAnnualLeave(remainAnnual)
				.annualLeaveDef(annualDef)
				.usedAnnualLeave(usedAnnual == null ? 0d : usedAnnual)
				.build();
	}

	@Override
	@Transactional(readOnly = true)
	public List<AtndHistoryResponse> getMyHistory(int days) {
		Long empId = SecurityUtil.getCurrentEmpId();
		LocalDate today = LocalDate.now();
		int span = days <= 0 ? 30 : days;
		LocalDate from = today.minusDays(span - 1L);
		return attendanceMapper.selectMyHistory(empId, from, today);
	}

	@Override
	@Transactional(readOnly = true)
	public List<AtndHistoryResponse> getMyMonth(String ym) {
		Long empId = SecurityUtil.getCurrentEmpId();
		LocalDate base;
		try {
			base = (ym == null || ym.isBlank())
					? LocalDate.now()
					: LocalDate.parse(ym + "-01");
		} catch (Exception e) {
			base = LocalDate.now();
		}
		LocalDate from = base.withDayOfMonth(1);
		LocalDate to = base.with(TemporalAdjusters.lastDayOfMonth());
		return attendanceMapper.selectMyHistory(empId, from, to);
	}

	// ============================ 관리자 ============================

	@Override
	@Transactional(readOnly = true)
	public AtndPolicyResponse getActivePolicy() {
		AtndPolicyVO vo = attendanceMapper.selectActivePolicy(LocalDate.now());
		return vo == null ? null : toPolicyResponse(vo);
	}

	@Override
	@Transactional
	public AtndPolicyResponse savePolicy(AtndPolicySaveRequest request) {
		assertPolicyManage();
		Long empId = SecurityUtil.getCurrentEmpId();
		LocalDate today = LocalDate.now();
		LocalDateTime now = LocalDateTime.now();

		// 기존 유효 정책 종료(전일자로 닫음 → 과거 근태는 과거 정책으로 유지)
		attendanceMapper.closeActivePolicies(today.minusDays(1), empId, now);

		AtndPolicyVO vo = new AtndPolicyVO();
		vo.setPolicyNm(request.getPolicyNm());
		vo.setDeptCd(null);
		vo.setWorkBgnTm(request.getWorkBgnTm());
		vo.setWorkEndTm(request.getWorkEndTm());
		vo.setBreakMin(request.getBreakMin());
		vo.setLateGraceMin(request.getLateGraceMin());
		vo.setStdWorkMinDay(request.getStdWorkMinDay());
		vo.setStdWorkDaysWk(request.getStdWorkDaysWk());
		vo.setStdWorkMinWk(request.getStdWorkMinWk());
		vo.setMaxOtMinWk(request.getMaxOtMinWk());
		vo.setOtUnitMin(request.getOtUnitMin());
		vo.setAnnualLeaveDef(request.getAnnualLeaveDef());
		vo.setEffBgnYmd(today);
		vo.setFrstRgtrId(empId);
		attendanceMapper.insertPolicy(vo);

		return toPolicyResponse(vo);
	}

	@Override
	@Transactional(readOnly = true)
	public List<AdminAtndRowResponse> getAllAttendance(String date, String deptCd, String keyword) {
		assertViewAll();
		LocalDate atndDt = StringUtils.hasText(date) ? LocalDate.parse(date) : LocalDate.now();
		return attendanceMapper.selectAllAttendance(atndDt, deptCd, keyword);
	}

	// ============================ 내부 헬퍼 ============================

	private AtndPolicyVO requireActivePolicy(LocalDate baseDate) {
		AtndPolicyVO policy = attendanceMapper.selectActivePolicy(baseDate);
		if (policy == null) {
			throw new CustomException(ErrorCode.ATND_POLICY_NOT_FOUND);
		}
		return policy;
	}

	private void assertPolicyManage() {
		if (!authorizationService.hasGlobalScope(PermissionCode.ATTENDANCE_POLICY_MANAGE)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void assertViewAll() {
		if (!authorizationService.hasGlobalScope(PermissionCode.ATTENDANCE_VIEW_ALL)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}
	}

	private AtndPolicyResponse toPolicyResponse(AtndPolicyVO vo) {
		return AtndPolicyResponse.builder()
				.atndPolicyId(vo.getAtndPolicyId())
				.policyNm(vo.getPolicyNm())
				.workBgnTm(vo.getWorkBgnTm())
				.workEndTm(vo.getWorkEndTm())
				.breakMin(vo.getBreakMin())
				.lateGraceMin(vo.getLateGraceMin())
				.stdWorkMinDay(vo.getStdWorkMinDay())
				.stdWorkDaysWk(vo.getStdWorkDaysWk())
				.stdWorkMinWk(vo.getStdWorkMinWk())
				.maxOtMinWk(vo.getMaxOtMinWk())
				.otUnitMin(vo.getOtUnitMin())
				.annualLeaveDef(vo.getAnnualLeaveDef())
				.effBgnYmd(vo.getEffBgnYmd())
				.effEndYmd(vo.getEffEndYmd())
				.build();
	}

	private String statNm(String statCd) {
		if (statCd == null) {
			return null;
		}
		String nm = attendanceMapper.selectStatNm(statCd);
		return nm != null ? nm : statCd;
	}

	private LocalTime parseTime(String hhmm) {
		return LocalTime.parse(hhmm, HM);
	}

	/** from 이후 to 까지 경과 분(to가 from보다 앞이면 0) */
	private int minutesAfter(LocalDateTime from, LocalDateTime to) {
		long min = Duration.between(from, to).toMinutes();
		return min > 0 ? (int) min : 0;
	}

	private int nz(Integer v) {
		return v == null ? 0 : v;
	}
}
