package com.mycrewsoft.domain.reservation.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회의실 예약 등록 요청 DTO")
public class ReservationCreateRequest {

    @NotNull
    @Schema(description = "회의실 ID", example = "3")
    private Long roomId;

    @NotBlank
    @Schema(description = "예약 제목", example = "주간 회의")
    private String title;

    @NotNull
    @Schema(description = "종일 예약 여부", example = "N")
    private String intgRsrvYn;
    
    @NotNull
    @Schema(description = "예약 시작 일시", example = "2026-06-05T09:00:00")
    private LocalDateTime startDateTime;

    @NotNull
    @Schema(description = "예약 종료 일시", example = "2026-06-05T09:30:00")
    private LocalDateTime endDateTime;
}