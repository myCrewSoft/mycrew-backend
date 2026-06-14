package com.mycrewsoft.domain.attendance.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.approval.dto.request.ApprovalDraftRequestDTO;
import com.mycrewsoft.domain.approval.service.ApprovalDraftWriteService;
import com.mycrewsoft.domain.approval.service.ApprovalRequestService;
import com.mycrewsoft.domain.attendance.dto.request.LeaveApplyRequest;
import com.mycrewsoft.domain.attendance.dto.request.LeaveGrantRequest;
import com.mycrewsoft.domain.attendance.dto.request.OtApplyRequest;
import com.mycrewsoft.domain.attendance.dto.response.AtndLeaveTypeResponse;
import com.mycrewsoft.domain.attendance.dto.response.LeaveBalanceResponse;
import com.mycrewsoft.domain.attendance.dto.response.MyLeaveResponse;
import com.mycrewsoft.domain.attendance.dto.response.MyOtResponse;
import com.mycrewsoft.domain.attendance.mapper.AttendanceMapper;
import com.mycrewsoft.domain.attendance.vo.AtndLeaveReqVO;
import com.mycrewsoft.domain.attendance.vo.AtndLeaveTypeVO;
import com.mycrewsoft.domain.attendance.vo.AtndOtReqVO;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceLeaveServiceImpl implements AttendanceLeaveService {

	private final AttendanceMapper attendanceMapper;
	private final ApprovalDraftWriteService approvalDraftWriteService;
	private final ApprovalRequestService approvalRequestService;
	private final AuthorizationService authorizationService;

	private static final String LEDGER_GRANT = "01";
	private static final String LEDGER_USE = "02";
	private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

	@Override
	@Transactional(readOnly = true)
	public List<AtndLeaveTypeResponse> getLeaveTypes() {
		return attendanceMapper.selectLeaveTypes();
	}

	@Override
	@Transactional(readOnly = true)
	public List<MyLeaveResponse> getMyLeaves() {
		return attendanceMapper.selectMyLeaves(SecurityUtil.getCurrentEmpId());
	}

	@Override
	@Transactional
	public Long applyLeave(LeaveApplyRequest request) {
		Long empId = SecurityUtil.getCurrentEmpId();

		AtndLeaveTypeVO type = attendanceMapper.selectLeaveType(request.getLeaveTypeCd());
		if (type == null) {
			throw new CustomException(ErrorCode.ATND_LEAVE_TYPE_NOT_FOUND);
		}

		LocalDate bgn = request.getLeaveBgnYmd();
		LocalDate end = request.getLeaveEndYmd();
		if (end.isBefore(bgn)) {
			throw new CustomException(ErrorCode.ATND_LEAVE_INVALID_RANGE);
		}

		boolean half = type.getHalfDayCd() != null;
		double perDay = type.getDeductDay() == null ? (half ? 0.5 : 1.0) : type.getDeductDay();
		double dayCnt = half ? perDay : countWeekdays(bgn, end) * perDay;

		ApprovalDraftRequestDTO draft = new ApprovalDraftRequestDTO();
		draft.setDocTtl(request.getDocTtl());
		draft.setTmplatCd(request.getTmplatCd());
		draft.setAprvlFullCn(request.getAprvlFullCn());
		draft.setAprvlHopeDt(request.getAprvlHopeDt());
		draft.setAtchFileId(request.getAtchFileId());
		draft.setApprovalLines(request.getApprovalLines());
		Long drftDocSn = approvalDraftWriteService.saveTemporaryDraft(draft);

		AtndLeaveReqVO leaveReq = new AtndLeaveReqVO();
		leaveReq.setEmpId(empId);
		leaveReq.setLeaveTypeCd(type.getLeaveTypeCd());
		leaveReq.setDrftDocSn(drftDocSn);
		leaveReq.setLeaveBgnDtm(bgn.atStartOfDay());
		leaveReq.setLeaveEndDtm(end.atTime(23, 59));
		leaveReq.setLeaveDayCnt(dayCnt);
		leaveReq.setReqRsn(request.getReqRsn());
		attendanceMapper.insertLeaveReq(leaveReq);

		approvalRequestService.submitApproval(drftDocSn);
		return drftDocSn;
	}

	@Override
	@Transactional
	public Long applyOt(OtApplyRequest request) {
		Long empId = SecurityUtil.getCurrentEmpId();
		LocalDate otDt = request.getOtYmd();
		LocalDateTime otBgnDtm = otDt.atTime(parseTime(request.getOtBgnTm()));
		LocalDateTime otEndDtm = otDt.atTime(parseTime(request.getOtEndTm()));
		if (!otEndDtm.isAfter(otBgnDtm)) {
			throw new CustomException(ErrorCode.ATND_OT_INVALID_RANGE);
		}

		ApprovalDraftRequestDTO draft = new ApprovalDraftRequestDTO();
		draft.setDocTtl(request.getDocTtl());
		draft.setTmplatCd(request.getTmplatCd());
		draft.setAprvlFullCn(request.getAprvlFullCn());
		draft.setAprvlHopeDt(request.getAprvlHopeDt());
		draft.setAtchFileId(request.getAtchFileId());
		draft.setApprovalLines(request.getApprovalLines());
		Long drftDocSn = approvalDraftWriteService.saveTemporaryDraft(draft);

		AtndOtReqVO otReq = new AtndOtReqVO();
		otReq.setEmpId(empId);
		otReq.setOtDt(otDt);
		otReq.setOtBgnDtm(otBgnDtm);
		otReq.setOtEndDtm(otEndDtm);
		otReq.setDrftDocSn(drftDocSn);
		attendanceMapper.insertOtReq(otReq);

		approvalRequestService.submitApproval(drftDocSn);
		return drftDocSn;
	}

	@Override
	@Transactional(readOnly = true)
	public List<MyOtResponse> getMyOts() {
		return attendanceMapper.selectMyOts(SecurityUtil.getCurrentEmpId());
	}

	@Override
	@Transactional
	public void reflectApprovedDocument(Long drftDocSn) {
		AtndLeaveReqVO leaveReq = attendanceMapper.selectLeaveReqByDoc(drftDocSn);
		if (leaveReq != null) {
			if (!"Y".equals(leaveReq.getRflctYn())) {
				reflectLeave(leaveReq);
			}
			return;
		}

		AtndOtReqVO otReq = attendanceMapper.selectOtReqByDoc(drftDocSn);
		if (otReq == null || "Y".equals(otReq.getRflctYn())) {
			return;
		}
		reflectOt(otReq);
	}

	@Override
	@Transactional(readOnly = true)
	public List<LeaveBalanceResponse> getLeaveBalances(int baseYear, String keyword) {
		assertLeaveManage();
		List<LeaveBalanceResponse> rows = attendanceMapper.selectLeaveBalances(baseYear, keyword);

		// 정책상 기본 연차를 기준으로 잔여 = 기본 + 원장(부여 - 사용)
		com.mycrewsoft.domain.attendance.vo.AtndPolicyVO policy =
				attendanceMapper.selectActivePolicy(java.time.LocalDate.now());
		double baseDay = policy != null && policy.getAnnualLeaveDef() != null
				? policy.getAnnualLeaveDef()
				: 0d;
		for (LeaveBalanceResponse row : rows) {
			double ledger = row.getRemainDay() == null ? 0d : row.getRemainDay();
			row.setBaseDay(baseDay);
			row.setRemainDay(baseDay + ledger);
		}
		return rows;
	}

	@Override
	@Transactional
	public void grantAnnualLeave(LeaveGrantRequest request) {
		assertLeaveManage();
		attendanceMapper.insertLeaveLedger(
				request.getEmpId(),
				request.getBaseYear(),
				LEDGER_GRANT,
				request.getDays(),
				null,
				request.getRemark() != null ? request.getRemark() : "연차 부여");
	}

	private void reflectLeave(AtndLeaveReqVO req) {
		AtndLeaveTypeVO type = attendanceMapper.selectLeaveType(req.getLeaveTypeCd());
		boolean half = type != null && type.getHalfDayCd() != null;
		String statCd = half ? AttendanceConstants.STAT_HALF : AttendanceConstants.STAT_LEAVE;
		double perDay = type == null || type.getDeductDay() == null
				? (half ? 0.5 : 1.0)
				: type.getDeductDay();

		LocalDate cursor = req.getLeaveBgnDtm().toLocalDate();
		LocalDate end = req.getLeaveEndDtm().toLocalDate();
		while (!cursor.isAfter(end)) {
			DayOfWeek dow = cursor.getDayOfWeek();
			if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
				attendanceMapper.mergeLeaveDaily(req.getEmpId(), cursor, statCd, perDay, req.getLeaveReqId());
			}
			if (half) {
				break;
			}
			cursor = cursor.plusDays(1);
		}

		attendanceMapper.updateLeaveReqReflected(req.getLeaveReqId(), LocalDateTime.now());
		if (req.getLeaveDayCnt() != null && req.getLeaveDayCnt() > 0) {
			attendanceMapper.insertLeaveLedger(
					req.getEmpId(),
					req.getLeaveBgnDtm().getYear(),
					LEDGER_USE,
					-req.getLeaveDayCnt(),
					req.getLeaveReqId(),
					type != null ? type.getLeaveTypeNm() + " 사용" : "휴가 사용");
		}
	}

	private void reflectOt(AtndOtReqVO otReq) {
		int approvedMin = (int) Duration.between(otReq.getOtBgnDtm(), otReq.getOtEndDtm()).toMinutes();
		if (approvedMin <= 0) {
			throw new CustomException(ErrorCode.ATND_OT_INVALID_RANGE);
		}
		attendanceMapper.updateOtReqReflected(otReq.getOtReqId(), approvedMin, LocalDateTime.now());
		Integer approvedTotal = attendanceMapper.selectApprovedOtMin(otReq.getEmpId(), otReq.getOtDt());
		attendanceMapper.updateDailyApprovedOt(
				otReq.getEmpId(),
				otReq.getOtDt(),
				approvedTotal == null ? 0 : approvedTotal);
	}

	private int countWeekdays(LocalDate from, LocalDate to) {
		int count = 0;
		LocalDate d = from;
		while (!d.isAfter(to)) {
			DayOfWeek dow = d.getDayOfWeek();
			if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
				count += 1;
			}
			d = d.plusDays(1);
		}
		return Math.max(count, 1);
	}

	private LocalTime parseTime(String hhmm) {
		return LocalTime.parse(hhmm, HM);
	}

	private void assertLeaveManage() {
		if (!authorizationService.hasGlobalScope(PermissionCode.ATTENDANCE_POLICY_MANAGE)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}
	}
}
