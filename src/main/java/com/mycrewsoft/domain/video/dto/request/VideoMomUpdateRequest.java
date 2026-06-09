package com.mycrewsoft.domain.video.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회의록 수정 요청 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoMomUpdateRequest {

    @Schema(description = "수정된 회의록 내용")
    @NotBlank
    private String momCn;
}