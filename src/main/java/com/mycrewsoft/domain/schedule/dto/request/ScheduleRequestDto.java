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
@Schema(description = "일정 생성/수정 요청 DTO")
public class ScheduleRequestDto {

	@Schema(description = "일정 구분 코드", example = "C001")
	@NotBlank(message = "일정 분류 코드는 필수입니다.")
	private String schdClsfCd;
	
	@Schema(description = "일정명", example = "회사 창립일")
	@NotBlank(message = "일정명은 필수입니다.")
	@Size(max = 300, message = "일정명은 최대 300자까지 입력 가능합니다.")
	private String schdNm;		
	
	@Schema(description = "부서 일정일 때 해당 부서코드")
	private String deptCd;

	@Schema(description = "프로젝트 일정일 때 해당 프로젝트 ID")
	private Long projId;
	
	@Schema(description = "업무 일정일 때 해당 업무 ID")
	private Long taskId;
	
	@Schema(description = "일정 상세 내용", example = "회사가 창립된 날")
	@Size(max = 300, message = "일정명은 최대 4000자까지 입력 가능합니다.")
	private String schdDetailCn;
	
	@Schema(description = "시작 일시", example = "2026-06-21 10:00:00")
	@NotNull(message = "시작일시는 필수입니다.")
	private LocalDateTime beginDt;
	
	@Schema(description = "종료 일시", example = "2026-06-21 10:00:00")
	@NotNull(message = "종료일시는 필수입니다.")
	private LocalDateTime endDt;	
	
	@Schema(description = "종일 여부", example = "n")
	@NotBlank(message = "종일 여부는 필수입니다.")
	private String allDayYn;
	
	@Schema(description = "반복 여부", example = "n")
	@NotBlank(message = "반복 여부는 필수입니다.")
	private String reptYn;
	
	@Schema(description = "반복 타입", example = "02")
	private String reptTypeCd;
	
	@Schema(description = "반복 종료일", example = "2026-06-21 10:00:00")
	private LocalDateTime reptEndDt;
	
	@Schema(description = "공유 대상 목록")
    @Valid
    List<ScheduleTargetRequestDto> targets;
	
}
