package com.mycrewsoft.domain.attendance.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.attendance.dto.request.LeaveApplyRequest;
import com.mycrewsoft.domain.attendance.dto.request.OtApplyRequest;
import com.mycrewsoft.domain.attendance.dto.response.AtndCheckResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndHistoryResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndLeaveTypeResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndStatsResponse;
import com.mycrewsoft.domain.attendance.dto.response.AtndTodayResponse;
import com.mycrewsoft.domain.attendance.dto.response.MyLeaveResponse;
import com.mycrewsoft.domain.attendance.dto.response.MyOtResponse;
import com.mycrewsoft.domain.attendance.service.AttendanceLeaveService;
import com.mycrewsoft.domain.attendance.service.AttendanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "근태", description = "사원 근태 API")
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

	private final AttendanceService attendanceService;
	private final AttendanceLeaveService attendanceLeaveService;

	@Operation(summary = "출근 처리", description = "현재 로그인한 사원의 출근을 처리하고 근무 정책 기준으로 지각 여부를 산정합니다.")
	@PostMapping("/check-in")
	public ResponseEntity<ApiResponse<AtndCheckResponse>> checkIn(HttpServletRequest request) {
		AtndCheckResponse result = attendanceService.checkIn(clientIp(request));
		return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
	}

	@Operation(summary = "퇴근 처리", description = "현재 로그인한 사원의 퇴근을 처리하고 근무 시간, 연장근무, 조퇴 여부를 산정합니다.")
	@PostMapping("/check-out")
	public ResponseEntity<ApiResponse<AtndCheckResponse>> checkOut(HttpServletRequest request) {
		AtndCheckResponse result = attendanceService.checkOut(clientIp(request));
		return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
	}

	@Operation(summary = "오늘 근태 조회", description = "오늘 본인의 출퇴근 현황과 근태 상태를 조회합니다.")
	@GetMapping("/today")
	public ResponseEntity<ApiResponse<AtndTodayResponse>> getToday() {
		return ResponseEntity.ok(ApiResponse.success(attendanceService.getMyToday()));
	}

	@Operation(summary = "근태 통계 조회", description = "기간 구분(DAY/WEEK/MONTH/YEAR)에 따른 본인 근태 통계 지표를 조회합니다.")
	@GetMapping("/stats")
	public ResponseEntity<ApiResponse<AtndStatsResponse>> getStats(
			@RequestParam(name = "period", defaultValue = "WEEK") String period) {
		return ResponseEntity.ok(ApiResponse.success(attendanceService.getMyStats(period)));
	}

	@Operation(summary = "근태 이력 조회", description = "최근 N일간의 본인 일자별 근태 이력을 조회합니다.")
	@GetMapping("/history")
	public ResponseEntity<ApiResponse<List<AtndHistoryResponse>>> getHistory(
			@RequestParam(name = "days", defaultValue = "30") int days) {
		return ResponseEntity.ok(ApiResponse.success(attendanceService.getMyHistory(days)));
	}

	@Operation(summary = "월별 근태 조회", description = "지정한 월(YYYY-MM)의 본인 일자별 근태를 조회합니다. 달력 표시에 사용합니다.")
	@GetMapping("/month")
	public ResponseEntity<ApiResponse<List<AtndHistoryResponse>>> getMonth(
			@RequestParam(name = "ym", required = false) String ym) {
		return ResponseEntity.ok(ApiResponse.success(attendanceService.getMyMonth(ym)));
	}

	@Operation(summary = "휴가 종류 조회", description = "신청 가능한 휴가 종류 목록을 조회합니다.")
	@GetMapping("/leave/types")
	public ResponseEntity<ApiResponse<List<AtndLeaveTypeResponse>>> getLeaveTypes() {
		return ResponseEntity.ok(ApiResponse.success(attendanceLeaveService.getLeaveTypes()));
	}

	@Operation(summary = "내 휴가 신청 내역 조회", description = "본인이 신청한 휴가 내역과 결재 상태를 조회합니다.")
	@GetMapping("/leave")
	public ResponseEntity<ApiResponse<List<MyLeaveResponse>>> getMyLeaves() {
		return ResponseEntity.ok(ApiResponse.success(attendanceLeaveService.getMyLeaves()));
	}

	@Operation(summary = "휴가 신청", description = "전자결재 문서를 생성하고 휴가 신청을 결재 요청합니다. 최종 승인 시 근태에 자동 반영됩니다.")
	@PostMapping("/leave")
	public ResponseEntity<ApiResponse<Long>> applyLeave(@Valid @RequestBody LeaveApplyRequest request) {
		Long drftDocSn = attendanceLeaveService.applyLeave(request);
		return ResponseEntity.ok(ApiResponse.success("휴가 신청이 결재 요청되었습니다.", drftDocSn));
	}

	@Operation(summary = "초과근무 신청 내역 조회", description = "본인이 신청한 초과근무 결재 내역과 근태 반영 상태를 조회합니다.")
	@GetMapping("/ot")
	public ResponseEntity<ApiResponse<List<MyOtResponse>>> getMyOts() {
		return ResponseEntity.ok(ApiResponse.success(attendanceLeaveService.getMyOts()));
	}

	@Operation(summary = "초과근무 신청", description = "전자결재 문서를 생성하고 초과근무 사전 신청을 결재 요청합니다. 최종 승인 시 해당 일자의 승인 초과근무 시간에 반영됩니다.")
	@PostMapping("/ot")
	public ResponseEntity<ApiResponse<Long>> applyOt(@Valid @RequestBody OtApplyRequest request) {
		Long drftDocSn = attendanceLeaveService.applyOt(request);
		return ResponseEntity.ok(ApiResponse.success("초과근무 신청이 결재 요청되었습니다.", drftDocSn));
	}

	private String clientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			return xff.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
