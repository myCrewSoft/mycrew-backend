package com.mycrewsoft.domain.video.controller;

import com.mycrewsoft.common.response.ApiResponse;
import com.mycrewsoft.domain.video.dto.response.VideoTokenResponse;
import com.mycrewsoft.domain.video.service.VideoConfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "화상회의", description = "화상회의 입장 토큰 발급, 종료, 입퇴장 관리 API")
@RestController
@RequestMapping("/video-conferences")
@RequiredArgsConstructor
public class VideoConfController {

    private final VideoConfService videoConfService;

    // LiveKit 입장 토큰 발급
    @Operation(summary = "LiveKit 토큰 발급", description = "화상회의 입장에 필요한 LiveKit 토큰을 발급합니다.")
    @PostMapping("/{vconfId}/token")
    public ResponseEntity<ApiResponse<VideoTokenResponse>> issueToken(@PathVariable Long vconfId) {
        VideoTokenResponse response = videoConfService.issueToken(vconfId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 화상회의 퇴장 (입퇴장 로그 LEAV_DT 기록)
    @Operation(summary = "화상회의 퇴장", description = "참여자의 퇴장 시각을 기록합니다.")
    @PatchMapping("/{vconfId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveConf(@PathVariable Long vconfId) {
        videoConfService.leaveConf(vconfId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 화상회의 종료 (호스트 전용) - AI 회의록 초안 생성 트리거
    @Operation(summary = "화상회의 종료", description = "호스트가 화상회의를 종료합니다. AI 회의록 초안이 생성됩니다.")
    @PatchMapping("/{vconfId}/end")
    public ResponseEntity<ApiResponse<Void>> endConf(@PathVariable Long vconfId) {
        videoConfService.endConf(vconfId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}