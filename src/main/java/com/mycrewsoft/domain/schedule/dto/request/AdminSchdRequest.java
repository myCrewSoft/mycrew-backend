package com.mycrewsoft.domain.schedule.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "관리자 일정 등록/수정 요청 DTO")
public class AdminSchdRequest {

    @Schema(description = "일정 구분 코드", example = "C001")
    @NotBlank(message = "일정 분류 코드는 필수입니다.")
    private String schdClsfCd;

    @Schema(description = "일정명", example = "전사 워크샵")
    @NotBlank(message = "일정명은 필수입니다.")
    @Size(max = 300, message = "일정명은 최대 300자까지 입력 가능합니다.")
    private String schdNm;

    @Schema(description = "일정 상세 내용", example = "2박 3일 전사 워크샵")
    @Size(max = 4000, message = "일정 상세 내용은 최대 4000자까지 입력 가능합니다.")
    private String schdDetailCn;

    @Schema(description = "시작 일시", example = "2026-06-21T10:00:00")
    @NotNull(message = "시작일시는 필수입니다.")
    private LocalDateTime beginDt;

    @Schema(description = "종료 일시", example = "2026-06-21T18:00:00")
    @NotNull(message = "종료일시는 필수입니다.")
    private LocalDateTime endDt;

    @Schema(description = "종일 여부", example = "N")
    @NotBlank(message = "종일 여부는 필수입니다.")
    private String allDayYn;

    @Schema(description = "반복 여부", example = "N")
    @NotBlank(message = "반복 여부는 필수입니다.")
    private String reptYn;

    @Schema(description = "반복 타입 코드", example = "02")
    private String reptTypeCd;

    @Schema(description = "반복 종료일", example = "2026-12-31T23:59:59")
    private LocalDateTime reptEndDt;

    @Schema(description = "공유 대상 목록")
    @Valid
    private List<ScheduleTargetRequestDto> targets;
}