package com.mycrewsoft.domain.video.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "화상회의 생성 요청 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoConfCreateRequest {

    @Schema(description = "회의 이름", example = "3분기 전략회의")
    @NotBlank
    @Size(max = 100)
    private String vconfNm;

    @Schema(description = "시작 일시", example = "2025-09-01T10:00:00")
    @NotNull
    private LocalDateTime beginDt;

    @Schema(description = "종료 일시", example = "2025-09-01T11:00:00")
    @NotNull
    private LocalDateTime endDt;

    @Schema(description = "초대할 사원 ID 목록", example = "[2, 3, 4]")
    @NotNull
    private List<Long> ptcptEmpIds;
}