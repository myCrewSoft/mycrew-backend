package com.mycrewsoft.domain.video.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회의록 결재 요청 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoMomAprvlRequest {

    @Schema(description = "결재 상태 코드 (01: 승인 / 02: 반려)", example = "01")
    @NotBlank
    private String aprvlSttusCd;
}