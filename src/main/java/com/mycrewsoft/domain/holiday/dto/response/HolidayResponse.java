package com.mycrewsoft.domain.holiday.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "공휴일 응답 DTO")
public class HolidayResponse {

    @Schema(description = "공휴일 ID", example = "1")
    private Long holidayId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "공휴일 날짜", example = "2026-01-01")
    private LocalDate holidayDt;

    @Schema(description = "공휴일명", example = "신정")
    private String holidayNm;

    @Schema(description = "공공기관 휴일 여부", example = "Y")
    private String isHolidayYn;

    @Schema(description = "생성 방식", example = "API")
    private String genTypeCd;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "API 동기화 일시")
    private LocalDateTime syncDt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "최초 등록 일시")
    private LocalDateTime frstRegDt;
}