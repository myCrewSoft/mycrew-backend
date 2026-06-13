package com.mycrewsoft.domain.attendance.service;

import java.util.List;

import com.mycrewsoft.domain.attendance.dto.request.LeaveApplyRequest;
import com.mycrewsoft.domain.attendance.dto.request.LeaveGrantRequest;
import com.mycrewsoft.domain.attendance.dto.request.OtApplyRequest;
import com.mycrewsoft.domain.attendance.dto.response.AtndLeaveTypeResponse;
import com.mycrewsoft.domain.attendance.dto.response.LeaveBalanceResponse;
import com.mycrewsoft.domain.attendance.dto.response.MyLeaveResponse;
import com.mycrewsoft.domain.attendance.dto.response.MyOtResponse;

/**
 * 휴가 신청·취합 서비스.
 * 신청은 전자결재 문서 생성 + 결재요청과 함께 이루어지고,
 * 최종 승인 시 근태(TB_ATND_DAILY)와 연차 원장(TB_ATND_LEAVE_LEDGER)으로 취합된다.
 */
public interface AttendanceLeaveService {

	/** 사용 가능한 휴가 종류 목록 */
	List<AtndLeaveTypeResponse> getLeaveTypes();

	/** 내 휴가 신청 내역 */
	List<MyLeaveResponse> getMyLeaves();

	/** 휴가 신청(전자결재 문서 생성 + 결재요청 + 휴가 신청 연동). 생성된 기안 문서 번호 반환 */
	Long applyLeave(LeaveApplyRequest request);

	/** 전자결재 최종 승인 문서를 근태로 취합(휴가 신청과 연결된 경우에만). */
	void reflectApprovedDocument(Long drftDocSn);

	// ===== 관리자: 연차 부여 =====

	/** 사원별 연차 현황(부여/사용/잔여) 조회 */
	List<LeaveBalanceResponse> getLeaveBalances(int baseYear, String keyword);

	/** 연차 부여(원장에 부여 행 적립) */
	void grantAnnualLeave(LeaveGrantRequest request);

	// ===== 초과근무 사전 신청 =====

	/** 초과근무 사전 신청(전자결재 문서 생성 + 결재요청). 생성된 기안 문서 번호 반환 */
	Long applyOt(OtApplyRequest request);

	/** 내 초과근무 신청 내역 */
	List<MyOtResponse> getMyOts();
}
