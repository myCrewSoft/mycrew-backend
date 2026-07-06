package com.mycrewsoft.domain.holiday.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "수동 공휴일 등록/수정 요청 DTO")
public class HolidayManualRequest {

    @Schema(description = "공휴일 날짜", example = "2026-08-15")
    @NotNull(message = "공휴일 날짜는 필수입니다.")
    private LocalDate holidayDt;

    @Schema(description = "공휴일명", example = "창립기념일")
    @NotBlank(message = "공휴일명은 필수입니다.")
    @Size(max = 100, message = "공휴일명은 최대 100자까지 입력 가능합니다.")
    private String holidayNm;

    @Schema(description = "공공기관 휴일 여부", example = "Y")
    @NotBlank(message = "휴일 여부는 필수입니다.")
    private String isHolidayYn;
}