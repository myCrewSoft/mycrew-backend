package com.mycrewsoft.domain.holiday.controller;

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
import com.mycrewsoft.domain.holiday.dto.request.HolidayManualRequest;
import com.mycrewsoft.domain.holiday.dto.response.HolidayResponse;
import com.mycrewsoft.domain.holiday.service.AdminHolidayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Holiday", description = "관리자 공휴일 관리 API")
@RestController
@RequestMapping("/admin/holidays")
@RequiredArgsConstructor
public class AdminHolidayController {

    private final AdminHolidayService adminHolidayService;

    @Operation(summary = "공휴일 목록 조회", description = "연도별 공휴일 목록을 반환합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getHolidayList(
            @RequestParam int year) {

        List<HolidayResponse> response = adminHolidayService.getHolidayList(year);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공휴일 단건 조회")
    @GetMapping("/{holidayId}")
    public ResponseEntity<ApiResponse<HolidayResponse>> getHoliday(
            @PathVariable Long holidayId) {

        HolidayResponse response = adminHolidayService.getHoliday(holidayId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공휴일 API 동기화", description = "한국천문연구원 API에서 해당 연도 공휴일을 동기화합니다.")
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Integer>> syncHolidays(
            @RequestParam int year) {

        int count = adminHolidayService.syncHolidays(year);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @Operation(summary = "공휴일 수동 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createHoliday(
            @Valid @RequestBody HolidayManualRequest request) {

        Long holidayId = adminHolidayService.createHoliday(request);

        return ResponseEntity.ok(ApiResponse.success(holidayId));
    }

    @Operation(summary = "공휴일 수정", description = "수동 등록된 공휴일만 수정 가능합니다.")
    @PutMapping("/{holidayId}")
    public ResponseEntity<ApiResponse<Void>> modifyHoliday(
            @PathVariable Long holidayId,
            @Valid @RequestBody HolidayManualRequest request) {

        adminHolidayService.modifyHoliday(holidayId, request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "공휴일 삭제", description = "수동 등록된 공휴일만 삭제 가능합니다.")
    @DeleteMapping("/{holidayId}")
    public ResponseEntity<ApiResponse<Void>> deleteHoliday(
            @PathVariable Long holidayId) {

        adminHolidayService.deleteHoliday(holidayId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}