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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Job API", description = "관리자 직급 관리 API")
public class AdminJobController {

    private final AdminJobService adminJobService;

    @Operation(summary = "직급 목록 불러오기", description = "직급 목록을 불러옵니다.")
    @GetMapping("/ranks")
    public ApiResponse<List<RankResponseDTO>> getRanks() {
        return ApiResponse.success(adminJobService.getRanks());
    }

    @Operation(summary = "직급 생성", description = "직급을 생성합니다.")
    @PostMapping("/ranks")
    public ApiResponse<RankResponseDTO> createRank(@Valid @RequestBody RankCreateRequestDTO request) {
        return ApiResponse.success(adminJobService.createRank(request));
    }

    @Operation(summary = "직급 수정", description = "직급을 수정합니다.")
    @PutMapping("/ranks/{rankId}")
    public ApiResponse<RankResponseDTO> updateRank(
            @PathVariable String rankId,
            @Valid @RequestBody RankUpdateRequestDTO request) {
        return ApiResponse.success(adminJobService.updateRank(rankId, request));
    }

    @Operation(summary = "직급 삭제", description = "직급을 삭제합니다. 삭제 시 해당 직급에 속한 직원들은 대체 직급으로 이동됩니다.")
    @DeleteMapping("/ranks/{rankId}")
    public ApiResponse<String> deleteRank(
            @PathVariable String rankId,
            @RequestBody(required = false) RankDeleteRequestDTO request) {
        adminJobService.deleteRank(rankId, request);
        return ApiResponse.success("Rank deleted successfully.");
    }

    @Operation(summary = "직급 부여", description = "직급을 다수의 직원에게 부여합니다.")
    @PostMapping("/ranks/{rankId}/assignments")
    public ApiResponse<RankResponseDTO> assignRank(
            @PathVariable String rankId,
            @Valid @RequestBody RankAssignRequestDTO request) {
        return ApiResponse.success(adminJobService.assignRank(rankId, request));
    }

    @Operation(summary = "직급 회수", description = "선택된 사원들의 직급을 회수합니다.")
    @DeleteMapping("/ranks/{rankId}/assignments")
    public ApiResponse<RankResponseDTO> revokeRank(
            @PathVariable String rankId,
            @Valid @RequestBody RankRevokeRequestDTO request) {
        return ApiResponse.success(adminJobService.revokeRank(rankId, request));
    }
}
