package com.mycrewsoft.domain.attendance.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.attendance.dto.response.AdminAtndRowResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndHistoryResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndLeaveTypeResponse;
import com.mycrewsoft.domain.attendance.dto.response.LeaveBalanceResponse;
import com.mycrewsoft.domain.attendance.dto.response.MyLeaveResponse;
import com.mycrewsoft.domain.attendance.dto.response.MyOtResponse;
import com.mycrewsoft.domain.attendance.vo.AtndDailyVO;
import com.mycrewsoft.domain.attendance.vo.AtndLeaveReqVO;
import com.mycrewsoft.domain.attendance.vo.AtndLeaveTypeVO;
import com.mycrewsoft.domain.attendance.vo.AtndOtReqVO;
import com.mycrewsoft.domain.attendance.vo.AtndPolicyVO;
import com.mycrewsoft.domain.attendance.vo.AtndStatsAggVO;

@Mapper
public interface AttendanceMapper {

	// ===== 근무 정책 =====

	/** 기준일에 유효한 전사 공통 근무 정책 조회(없으면 null) */
	AtndPolicyVO selectActivePolicy(@Param("baseDate") LocalDate baseDate);

	/** 현재 유효한 전사 공통 정책들을 종료 처리(EFF_END_YMD 설정 + USE_YN='N') */
	int closeActivePolicies(
			@Param("endYmd") LocalDate endYmd,
			@Param("mdfrId") Long mdfrId,
			@Param("now") LocalDateTime now);

	/** 새 근무 정책 버전 등록 */
	void insertPolicy(AtndPolicyVO policy);

	// ===== 일일 근태 =====

	/** 사원·일자 일일 근태 조회(없으면 null) */
	AtndDailyVO selectDaily(
			@Param("empId") Long empId,
			@Param("atndDt") LocalDate atndDt);

	/** 일일 근태 신규 등록(출근) */
	void insertDaily(AtndDailyVO daily);

	/** 일일 근태 갱신(퇴근/판정 결과 반영) */
	int updateDaily(AtndDailyVO daily);

	/** 본인 근태 이력 조회(기간) */
	List<AtndHistoryResponse> selectMyHistory(
			@Param("empId") Long empId,
			@Param("from") LocalDate from,
			@Param("to") LocalDate to);

	/** 본인 근태 기간 집계 */
	AtndStatsAggVO selectStatsAgg(
			@Param("empId") Long empId,
			@Param("from") LocalDate from,
			@Param("to") LocalDate to);

	// ===== 코드/연차 =====

	/** 근태 상태 코드명 조회 */
	String selectStatNm(@Param("statCd") String statCd);

	/** 귀속연도 잔여 연차(원장 합계 = 부여 - 사용) 조회 */
	Double selectRemainAnnualLeave(
			@Param("empId") Long empId,
			@Param("baseYear") int baseYear);

	/** 귀속연도 사용 연차(원장 사용 행 합계) 조회 */
	Double selectUsedAnnualLeave(
			@Param("empId") Long empId,
			@Param("baseYear") int baseYear);

	// ===== 관리자 =====

	/** 전체 사원 근태 현황 조회(일자/부서/키워드 필터) */
	List<AdminAtndRowResponse> selectAllAttendance(
			@Param("atndDt") LocalDate atndDt,
			@Param("deptCd") String deptCd,
			@Param("keyword") String keyword);

	// ===== 휴가 =====

	/** 사용 가능한 휴가 종류 목록 조회 */
	List<AtndLeaveTypeResponse> selectLeaveTypes();

	/** 휴가 종류 단건 조회 */
	AtndLeaveTypeVO selectLeaveType(@Param("cd") String cd);

	/** 휴가 신청 등록 */
	void insertLeaveReq(AtndLeaveReqVO leaveReq);

	/** 전자결재 문서로 연결된 휴가 신청 조회(없으면 null) */
	AtndLeaveReqVO selectLeaveReqByDoc(@Param("drftDocSn") Long drftDocSn);

	/** 휴가 신청을 승인·반영 처리 */
	int updateLeaveReqReflected(
			@Param("leaveReqId") Long leaveReqId,
			@Param("now") LocalDateTime now);

	/** 휴가일을 일일 근태에 반영(MERGE) */
	void mergeLeaveDaily(
			@Param("empId") Long empId,
			@Param("atndDt") LocalDate atndDt,
			@Param("statCd") String statCd,
			@Param("leaveDay") Double leaveDay,
			@Param("leaveReqId") Long leaveReqId);

	/** 연차 원장 등록(부여/사용/조정) */
	void insertLeaveLedger(
			@Param("empId") Long empId,
			@Param("baseYear") int baseYear,
			@Param("ledgerSeCd") String ledgerSeCd,
			@Param("leaveDay") Double leaveDay,
			@Param("srcLeaveReqId") Long srcLeaveReqId,
			@Param("rmrk") String rmrk);

	/** 내 휴가 신청 내역 조회 */
	List<MyLeaveResponse> selectMyLeaves(@Param("empId") Long empId);

	/** 사원별 연차 현황(부여/사용/잔여) 조회 */
	List<LeaveBalanceResponse> selectLeaveBalances(
			@Param("baseYear") int baseYear,
			@Param("keyword") String keyword);

	// ===== 초과근무 =====

	/** 초과근무 신청 등록 */
	void insertOtReq(AtndOtReqVO otReq);

	/** 전자결재 문서로 연결된 초과근무 신청 조회(없으면 null) */
	AtndOtReqVO selectOtReqByDoc(@Param("drftDocSn") Long drftDocSn);

	/** 초과근무 신청을 승인·반영 처리(승인 분 기록) */
	int updateOtReqReflected(
			@Param("otReqId") Long otReqId,
			@Param("approvedMin") int approvedMin,
			@Param("now") LocalDateTime now);

	/** 특정 일자의 승인된 초과근무 합계(분) */
	Integer selectApprovedOtMin(
			@Param("empId") Long empId,
			@Param("otDt") LocalDate otDt);

	/** 일일 근태의 승인근무/초과근무를 승인분 기준으로 재계산 */
	int updateDailyApprovedOt(
			@Param("empId") Long empId,
			@Param("atndDt") LocalDate atndDt,
			@Param("approvedMin") int approvedMin);

	/** 내 초과근무 신청 내역 조회 */
	List<MyOtResponse> selectMyOts(@Param("empId") Long empId);
}
