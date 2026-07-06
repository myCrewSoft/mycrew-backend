package com.mycrewsoft.domain.task.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "대시보드용 마감일 임박 업무 목록 조회 응답 DTO")
public class TaskUpcomingResponse {

	@Schema(description = "업무 ID", example = "1")
	private Long taskId;

    @Schema(description = "업무명", example = "결제 게이트웨이 UI 컴포넌트 개발")
    private String taskNm;

    @Schema(description = "업무담당자 이름 (JOIN)", example = "김민준")
    private String taskMngrNm;

    @Schema(description = "업무 종료일시(마감일)")
    private LocalDate taskEndDt;

    @Schema(description = "업무우선순위코드", example = "01")
    private String taskPriorityCd;

    @Schema(description = "남은 날짜", example = "1")
    private String dDay;
}
