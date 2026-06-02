package com.mycrewsoft.domain.jobgrade.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.jobgrade.dto.request.RankAssignRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankCreateRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankDeleteRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankRevokeRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.request.RankUpdateRequestDTO;
import com.mycrewsoft.domain.jobgrade.dto.response.RankResponseDTO;
import com.mycrewsoft.domain.jobgrade.service.AdminJobService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminJobController {

    private final AdminJobService adminJobService;

    @Operation(summary = "Get ranks", description = "Returns rank list ordered by sort order.")
    @GetMapping("/ranks")
    public ApiResponse<List<RankResponseDTO>> getRanks() {
        return ApiResponse.success(adminJobService.getRanks());
    }

    @Operation(summary = "Create rank", description = "Creates a rank with sort order.")
    @PostMapping("/ranks")
    public ApiResponse<RankResponseDTO> createRank(@Valid @RequestBody RankCreateRequestDTO request) {
        return ApiResponse.success(adminJobService.createRank(request));
    }

    @Operation(summary = "Update rank", description = "Updates rank name and sort order.")
    @PutMapping("/ranks/{rankId}")
    public ApiResponse<RankResponseDTO> updateRank(
            @PathVariable String rankId,
            @Valid @RequestBody RankUpdateRequestDTO request) {
        return ApiResponse.success(adminJobService.updateRank(rankId, request));
    }

    @Operation(summary = "Delete rank", description = "Disables a rank after moving assigned employees to replacement rank when needed.")
    @DeleteMapping("/ranks/{rankId}")
    public ApiResponse<String> deleteRank(
            @PathVariable String rankId,
            @RequestBody(required = false) RankDeleteRequestDTO request) {
        adminJobService.deleteRank(rankId, request);
        return ApiResponse.success("Rank deleted successfully.");
    }

    @Operation(summary = "Assign rank", description = "Assigns a rank to multiple employees.")
    @PostMapping("/ranks/{rankId}/assignments")
    public ApiResponse<RankResponseDTO> assignRank(
            @PathVariable String rankId,
            @Valid @RequestBody RankAssignRequestDTO request) {
        return ApiResponse.success(adminJobService.assignRank(rankId, request));
    }

    @Operation(summary = "Revoke rank", description = "Moves selected employees from a rank to a replacement rank.")
    @DeleteMapping("/ranks/{rankId}/assignments")
    public ApiResponse<RankResponseDTO> revokeRank(
            @PathVariable String rankId,
            @Valid @RequestBody RankRevokeRequestDTO request) {
        return ApiResponse.success(adminJobService.revokeRank(rankId, request));
    }
}
