package com.mycrewsoft.domain.room.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.room.dto.request.RoomCreateRequest;
import com.mycrewsoft.domain.room.dto.request.RoomUpdateRequest;
import com.mycrewsoft.domain.room.dto.response.RoomResponse;
import com.mycrewsoft.domain.room.service.RoomService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Room", description = "회의실 관련 API")
@RestController
@RequestMapping("/meeting-rooms")
@RequiredArgsConstructor
public class RoomController {

	private final RoomService roomService;
	
	@Operation(summary = "회의실 단건 조회")
	@GetMapping("/{confRmId}")
	public ResponseEntity<ApiResponse<RoomResponse>> getRoom(
			@PathVariable Long confRmId) {

		RoomResponse response = roomService.readRoom(confRmId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}
	
	@Operation(summary = "회의실 목록 조회")
	@GetMapping
	public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomList() {
		
		List<RoomResponse> response = roomService.readRoomList();
		
		return ResponseEntity.ok(ApiResponse.success(response));
	}
	
	@Operation(summary = "회의실 생성")
	@PostMapping
	public ResponseEntity<ApiResponse<Long>> postRoom(
		@Valid @RequestBody RoomCreateRequest dto
	) {
		
		Long confRmId = roomService.createRoom(dto);
		
		return ResponseEntity.ok(ApiResponse.success(confRmId));
	}
	
	@Operation(summary = "회의실 수정")
	@PutMapping("/{confRmId}")
	public ResponseEntity<ApiResponse<Void>> putRoom(
		@PathVariable Long confRmId,
		@Valid @RequestBody RoomUpdateRequest dto
	) {
		
		roomService.modifyRoom(dto);
		
		return ResponseEntity.ok(ApiResponse.success());
	}
	
	@Operation(summary = "회의실 삭제")
	@DeleteMapping("/{confRmId}")
	public ResponseEntity<ApiResponse<Void>> deleteRoom(
		@PathVariable Long confRmId
	) {
		
		roomService.deleteRoom(confRmId);
		
		return ResponseEntity.ok(ApiResponse.success());
	}
}
