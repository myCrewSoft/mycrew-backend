package com.mycrewsoft.domain.schedule.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "일정 응답 DTO")
public class ScheduleResponseDto {
	
	@Schema(description = "일정 일련번호", example = "0000000001")
	private Long schdId;
	
	@Schema(description = "일정 구분 코드", example = "C001")
	private String schdClsfCd;
	
	@Schema(description = "일정명", example = "회사 창립일")
	private String schdNm;		
	
	@Schema(description = "일정 상세 내용", example = "회사가 창립된 날")
	private String schdDetailCn;
	
	@Schema(description = "시작 일시", example = "2026-06-21 10:00:00")
	private LocalDateTime beginDt;
	
	@Schema(description = "종료 일시", example = "2026-06-21 10:00:00")
	private LocalDateTime endDt;	
	
	@Schema(description = "종일 일정 여부", example = "n")
    private String allDayYn;

    @Schema(description = "반복 일정 여부", example = "n")
    private String reptYn;

    @Schema(description = "반복 유형 코드", example = "02")
    private String reptTypeCd;

    @Schema(description = "반복 종료일")
    private LocalDateTime reptEndDt;
	
    @Schema(description = "공유 대상 목록")
    private List<ScheduleTargetResponseDto> targets;
}
