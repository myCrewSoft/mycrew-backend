package com.mycrewsoft.domain.video.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.video.dto.request.VideoConfCreateRequest;
import com.mycrewsoft.domain.video.dto.request.VideoMomAprvlRequest;
import com.mycrewsoft.domain.video.dto.request.VideoMomUpdateRequest;
import com.mycrewsoft.domain.video.dto.response.VideoConfResponse;
import com.mycrewsoft.domain.video.dto.response.VideoMomResponse;
import com.mycrewsoft.domain.video.dto.response.VideoTokenResponse;
import com.mycrewsoft.domain.video.service.VideoConfService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "화상회의", description = "화상회의 생성, 조회, 토큰 발급, 회의록 관리 API")
@RestController
@RequestMapping("/video-conferences")
@RequiredArgsConstructor
public class VideoConfController {

    private final VideoConfService videoConfService;

    // 화상회의 생성 (참여자 등록 포함)
    @Operation(summary = "화상회의 생성", description = "화상회의를 생성하고 참여자를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<VideoConfResponse>> createConf(
            @Valid @RequestBody VideoConfCreateRequest request) {

        VideoConfResponse response = videoConfService.createConf(request);
        return ResponseEntity.ok(ApiResponse.success("화상회의가 생성되었습니다.", response));
    }

    // 내가 참여 중인 화상회의 목록 조회
    @Operation(summary = "화상회의 목록 조회", description = "로그인한 사원이 참여 중인 화상회의 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<VideoConfResponse>>> getConfList() {

        List<VideoConfResponse> response = videoConfService.getConfList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 화상회의 단건 조회
    @Operation(summary = "화상회의 단건 조회", description = "화상회의 상세 정보와 참여자 목록을 조회합니다.")
    @GetMapping("/{vconfId}")
    public ResponseEntity<ApiResponse<VideoConfResponse>> getConf(
            @PathVariable Long vconfId) {

        VideoConfResponse response = videoConfService.getConf(vconfId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // LiveKit 입장 토큰 발급
    @Operation(summary = "LiveKit 토큰 발급", description = "화상회의 입장에 필요한 LiveKit 토큰을 발급합니다.")
    @PostMapping("/{vconfId}/token")
    public ResponseEntity<ApiResponse<VideoTokenResponse>> issueToken(
            @PathVariable Long vconfId) {

        VideoTokenResponse response = videoConfService.issueToken(vconfId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 화상회의 종료 (호스트 전용)
    @Operation(summary = "화상회의 종료", description = "호스트가 화상회의를 종료합니다. 상태 코드가 03으로 변경됩니다.")
    @PatchMapping("/{vconfId}/end")
    public ResponseEntity<ApiResponse<Void>> endConf(
            @PathVariable Long vconfId) {

        videoConfService.endConf(vconfId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 회의록 조회
    @Operation(summary = "회의록 조회", description = "화상회의의 회의록을 조회합니다. 결재 목록과 수정 이력을 포함합니다.")
    @GetMapping("/{vconfId}/minutes")
    public ResponseEntity<ApiResponse<VideoMomResponse>> getMom(
            @PathVariable Long vconfId) {

        VideoMomResponse response = videoConfService.getMom(vconfId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 회의록 수정 (수정 이력 자동 저장)
    @Operation(summary = "회의록 수정", description = "회의록 내용을 수정합니다. 수정 전 내용은 이력으로 자동 저장됩니다.")
    @PutMapping("/{vconfId}/minutes")
    public ResponseEntity<ApiResponse<VideoMomResponse>> updateMom(
            @PathVariable Long vconfId,
            @Valid @RequestBody VideoMomUpdateRequest request) {

        VideoMomResponse response = videoConfService.updateMom(vconfId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 참여자 전원에게 회의록 검토 요청 발송
    @Operation(summary = "회의록 검토 요청", description = "참여자 전원에게 회의록 검토 요청을 발송합니다. 새 결재 회차가 생성됩니다.")
    @PostMapping("/{vconfId}/minutes/review")
    public ResponseEntity<ApiResponse<String>> requestMomReview(
            @PathVariable Long vconfId) {

        videoConfService.requestMomReview(vconfId);
        return ResponseEntity.ok(ApiResponse.success("검토 요청이 발송되었습니다."));
    }

    // 회의록 결재 처리 (전원 승인 시 자동 확정)
    @Operation(summary = "회의록 결재", description = "회의록을 승인 또는 반려합니다. 참여자 전원 승인 시 회의록이 자동 확정됩니다.")
    @PostMapping("/{vconfId}/minutes/approve")
    public ResponseEntity<ApiResponse<Void>> approveMom(
            @PathVariable Long vconfId,
            @Valid @RequestBody VideoMomAprvlRequest request) {

        videoConfService.approveMom(vconfId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}