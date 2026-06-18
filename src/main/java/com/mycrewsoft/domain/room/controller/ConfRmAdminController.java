package com.mycrewsoft.domain.room.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.room.dto.response.ConfRmListItem;
import com.mycrewsoft.domain.room.dto.response.ConfRmStatsSummary;
import com.mycrewsoft.domain.room.service.ConfRmAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/rooms")
public class ConfRmAdminController {

    private final ConfRmAdminService confRmAdminService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ConfRmStatsSummary>> readConfRmStatsSummary() {
        return ResponseEntity.ok(ApiResponse.success(confRmAdminService.readConfRmStatsSummary()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConfRmListItem>>> readConfRmList() {
        return ResponseEntity.ok(ApiResponse.success(confRmAdminService.readConfRmList()));
    }
}