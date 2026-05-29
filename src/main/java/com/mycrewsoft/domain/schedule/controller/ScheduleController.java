package com.mycrewsoft.domain.schedule.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.schedule.dto.request.ScheduleRequestDto;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleResponseDto;
import com.mycrewsoft.domain.schedule.service.ScheduleService;
import com.mycrewsoft.security.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Schedule", description = "일정 관련 API")
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService schdService;

    @Operation(summary = "일정 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createSchd(
    		 @Valid @RequestBody ScheduleRequestDto dto) {

        Long schdId = schdService.createSchd(dto);
        
        return ResponseEntity.ok(ApiResponse.success(schdId));
    }

    @Operation(summary = "일정 목록 조회 (캘린더)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleResponseDto>>> getSchdList(
            @Parameter(description = "조회 시작 일시", example = "2026-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beginDt,
            @Parameter(description = "조회 종료 일시", example = "2026-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDt) {

        List<ScheduleResponseDto> schdList = schdService.readSchdList(beginDt, endDt);
        
        return ResponseEntity.ok(ApiResponse.success(schdList));
    }
}
