package com.mycrewsoft.domain.reservation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.reservation.dto.request.RsrvSearchRequest;
import com.mycrewsoft.domain.reservation.dto.response.RsrvListItem;
import com.mycrewsoft.domain.reservation.dto.response.RsrvStatsSummary;
import com.mycrewsoft.domain.reservation.service.RsrvAdminService;
import com.mycrewsoft.domain.reservation.vo.RsrvSearchVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/reservations")
public class RsrvAdminController {

	private final RsrvAdminService rsrvAdminService;

	@GetMapping("/stats")
	public ResponseEntity<ApiResponse<RsrvStatsSummary>> readRsrvStatsSummary() {
		return ResponseEntity.ok(ApiResponse.success(rsrvAdminService.readRsrvStatsSummary()));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<RsrvListItem>>> readRsrvList(@ModelAttribute RsrvSearchRequest request) {
		return ResponseEntity.ok(ApiResponse.success(rsrvAdminService.readRsrvList(request)));
	}

	@DeleteMapping("/{rsrvId}")
	public ResponseEntity<ApiResponse<Void>> cancelRsrv(@PathVariable Long rsrvId) {
		rsrvAdminService.cancelRsrv(rsrvId);
		return ResponseEntity.ok(ApiResponse.success());
	}
}