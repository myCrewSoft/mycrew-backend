package com.mycrewsoft.domain.reservation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.reservation.dto.request.ReservationCreateRequest;
import com.mycrewsoft.domain.reservation.dto.request.ReservationUpdateRequest;
import com.mycrewsoft.domain.reservation.dto.response.ReservationResponse;
import com.mycrewsoft.domain.reservation.service.ReservationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Reservation", description = "회의실 예약 관련 API")
@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {
	
	private final ReservationService reservationService;
	
	@Operation(summary = "예약 단건 조회")
	@GetMapping("/{rsrvId}")
	public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(
		@PathVariable Long rsrvId
	) {

		ReservationResponse response = reservationService.readReservation(rsrvId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}
	
	@Operation(summary = "예약 목록 조회")
	@GetMapping
	public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservationList(
		@RequestParam String begin,
		@RequestParam String end
	) {
		
		List<ReservationResponse> response = reservationService.readReservationList(begin, end);
		
		return ResponseEntity.ok(ApiResponse.success(response));
	}
	
	@Operation(summary = "예약 생성")
	@PostMapping
	public ResponseEntity<ApiResponse<Long>> postReservation(
		@Valid @RequestBody ReservationCreateRequest dto
	) {
		
		Long rsrvId = reservationService.createReservation(dto);
		
		return ResponseEntity.ok(ApiResponse.success(rsrvId));
	}
	
	@Operation(summary = "예약 수정")
	@PutMapping("/{rsrvId}")
	public ResponseEntity<ApiResponse<Void>> putReservation(
		@PathVariable Long rsrvId,
		@Valid @RequestBody ReservationUpdateRequest dto
	) {
		
		reservationService.modifyReservation(rsrvId, dto);
		
		return ResponseEntity.ok(ApiResponse.success());
	}
	
	@Operation(summary = "예약 삭제")
	@DeleteMapping("/{rsrvId}")
	public ResponseEntity<ApiResponse<Void>> deleteReservation(
		@PathVariable Long rsrvId
	) {
		
		reservationService.deleteReservation(rsrvId);
		
		return ResponseEntity.ok(ApiResponse.success());
	}
}
