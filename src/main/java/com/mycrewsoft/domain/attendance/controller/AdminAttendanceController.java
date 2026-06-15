package com.mycrewsoft.domain.attendance.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PathVariable;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.attendance.dto.request.AtndPolicySaveRequest;
import com.mycrewsoft.domain.attendance.dto.request.LeaveGrantRequest;
import com.mycrewsoft.domain.attendance.dto.response.AdminAtndRowResponse;
import com.mycrewsoft.domain.attendance.dto.response.AdminAtndStatResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndPolicyResponse;
import com.mycrewsoft.domain.attendance.dto.response.LeaveBalanceResponse;
import com.mycrewsoft.domain.attendance.service.AttendanceLeaveService;
import com.mycrewsoft.domain.attendance.service.AttendanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 근태 관리 API. 회사 근무 정책 설정과 전체 사원 근태 현황 조회, 연차 부여를 제공한다.
 */
@Tag(name = "AdminAttendance", description = "관리자 근태 관리 API (근무 정책/전체 현황/연차)")
@RestController
@RequestMapping("/admin/attendance")
@RequiredArgsConstructor
public class AdminAttendanceController {

	private final AttendanceService attendanceService;
	private final AttendanceLeaveService attendanceLeaveService;

	@Operation(summary = "근무 정책 조회", description = "현재 유효한 회사 근무 정책을 조회합니다. 설정된 정책이 없으면 data가 null로 반환됩니다.")
	@GetMapping("/policy")
	public ResponseEntity<ApiResponse<AtndPolicyResponse>> getPolicy() {
		return ResponseEntity.ok(ApiResponse.success(attendanceService.getActivePolicy()));
	}

	@Operation(summary = "근무 정책 저장", description = "회사 근무 정책을 저장합니다. 기존 정책은 종료 처리되고 새 버전이 생성됩니다. ATTENDANCE_POLICY_MANAGE 권한이 필요합니다.")
	@PostMapping("/policy")
	public ResponseEntity<ApiResponse<AtndPolicyResponse>> savePolicy(
			@Valid @RequestBody AtndPolicySaveRequest request) {
		AtndPolicyResponse result = attendanceService.savePolicy(request);
		return ResponseEntity.ok(ApiResponse.success("근무 정책이 저장되었습니다.", result));
	}

	@Operation(summary = "전체 사원 근태 현황 조회", description = "지정한 일자의 전체 사원 근태 현황을 조회합니다. 부서/사원명으로 필터링할 수 있으며 ATTENDANCE_VIEW_ALL 권한이 필요합니다.")
	@GetMapping
	public ResponseEntity<ApiResponse<List<AdminAtndRowResponse>>> getAllAttendance(
			@RequestParam(name = "date", required = false) String date,
			@RequestParam(name = "deptCd", required = false) String deptCd,
			@RequestParam(name = "keyword", required = false) String keyword) {
		return ResponseEntity.ok(ApiResponse.success(
				attendanceService.getAllAttendance(date, deptCd, keyword)));
	}

	@Operation(summary = "기간별 사원 근태 집계", description = "지정 기간(from~to) 동안 사원별 근무/지각/휴가 등을 집계해 조회합니다. ATTENDANCE_VIEW_ALL 권한이 필요합니다.")
	@GetMapping("/stats")
	public ResponseEntity<ApiResponse<List<AdminAtndStatResponse>>> getAttendanceStats(
			@RequestParam(name = "from", required = false) String from,
			@RequestParam(name = "to", required = false) String to,
			@RequestParam(name = "deptCd", required = false) String deptCd,
			@RequestParam(name = "keyword", required = false) String keyword) {
		return ResponseEntity.ok(ApiResponse.success(
				attendanceService.getAttendanceStats(from, to, deptCd, keyword)));
	}

	@Operation(summary = "사원 기간 근태 상세", description = "특정 사원의 기간 일자별 근태를 조회합니다. ATTENDANCE_VIEW_ALL 권한이 필요합니다.")
	@GetMapping("/employee/{empId}")
	public ResponseEntity<ApiResponse<List<AdminAtndRowResponse>>> getEmployeeAttendance(
			@PathVariable("empId") Long empId,
			@RequestParam(name = "from", required = false) String from,
			@RequestParam(name = "to", required = false) String to) {
		return ResponseEntity.ok(ApiResponse.success(
				attendanceService.getEmployeeAttendance(empId, from, to)));
	}

	// ===== 연차 부여 =====

	@Operation(summary = "사원별 연차 현황 조회", description = "귀속 연도 기준 사원별 연차 부여/사용/잔여 현황을 조회합니다. ATTENDANCE_POLICY_MANAGE 권한이 필요합니다.")
	@GetMapping("/leave/balances")
	public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getLeaveBalances(
			@RequestParam(name = "baseYear", required = false) Integer baseYear,
			@RequestParam(name = "keyword", required = false) String keyword) {
		int year = baseYear != null ? baseYear : java.time.LocalDate.now().getYear();
		return ResponseEntity.ok(ApiResponse.success(
				attendanceLeaveService.getLeaveBalances(year, keyword)));
	}

	@Operation(summary = "연차 부여", description = "특정 사원에게 연차를 부여(원장 적립)합니다. ATTENDANCE_POLICY_MANAGE 권한이 필요합니다.")
	@PostMapping("/leave/grant")
	public ResponseEntity<ApiResponse<Void>> grantLeave(@Valid @RequestBody LeaveGrantRequest request) {
		attendanceLeaveService.grantAnnualLeave(request);
		return ResponseEntity.ok(ApiResponse.success("연차가 부여되었습니다.", null));
	}
}
