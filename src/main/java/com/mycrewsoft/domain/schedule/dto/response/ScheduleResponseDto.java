package com.mycrewsoft.domain.schedule.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "일정 응답 DTO")
public class ScheduleResponseDto {

    @Schema(description = "일정 ID", example = "1")
    private Long id;

    @Schema(description = "일정 구분 코드", example = "C001")
    private String scheduleTypeCode;

    @Schema(description = "일정명", example = "회사 창립일")
    private String title;

    @Schema(description = "부서 일정일 때 해당 부서코드")
    private String deptCd;

    @Schema(description = "프로젝트 일정일 때 해당 프로젝트 ID")
    private Long projId;

    @Schema(description = "업무 일정일 때 해당 업무 ID")
    private Long taskId;

    @Schema(description = "작성자 사원 ID")
    private Long writerId;

    @Schema(description = "작성자 사원 이름")
    private String writerName;

    @Schema(description = "일정 상세 내용", example = "회사가 창립된 날")
    private String detail;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "시작 일시", example = "2026-06-21T10:00:00")
    private LocalDateTime start;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "종료 일시", example = "2026-06-21T11:00:00")
    private LocalDateTime end;

    @Schema(description = "종일 일정 여부", example = "false")
    private Boolean allDay;

    @Schema(description = "반복 일정 여부", example = "false")
    private Boolean repeat;

    @Schema(description = "반복 유형 코드", example = "02")
    private String repeatTypeCode;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "반복 종료일", example = "2026-06-30T23:59:59")
    private LocalDateTime repeatEndDate;

    @Schema(description = "공유 대상 목록")
    private List<ScheduleTargetResponseDto> targets;
}