package com.mycrewsoft.domain.task.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "대시보드용 업무 응답 DTO")
public class TaskDashboardResponse {

	@Schema(description = "대시보드용 업무 데이터 응답 DTO")
	private TaskDashboardSummaryResponse summary;

	@Schema(description = "대시보드용 마감일 임박 업무 목록 조회 응답 DTO")
    private List<TaskUpcomingResponse> upcomingTasks;
}
