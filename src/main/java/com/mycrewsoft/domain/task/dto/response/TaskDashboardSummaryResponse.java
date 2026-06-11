package com.mycrewsoft.domain.task.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "대시보드용 업무 데이터 응답 DTO")
public class TaskDashboardSummaryResponse {

	@Schema(description = "총 업무 수", example = "1")
	private int totalCount;        
	
	@Schema(description = "완료 업무 수", example = "1")
    private int completedCount;  
	
	@Schema(description = "진행 중 업무 수", example = "1")
    private int inProgressCount; 
	
	@Schema(description = "중지된 업무 수", example = "1")
    private int StopCount;       
}
