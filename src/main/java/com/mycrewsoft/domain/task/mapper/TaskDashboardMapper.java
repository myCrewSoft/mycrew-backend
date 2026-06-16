package com.mycrewsoft.domain.task.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.task.dto.response.TaskDashboardSummaryResponse;
import com.mycrewsoft.domain.task.dto.response.TaskUpcomingResponse;

@Mapper
public interface TaskDashboardMapper {

	TaskDashboardSummaryResponse selectTaskDashboardSummary(@Param("projId") Long projId);

	List<TaskUpcomingResponse> selectUpcomingTasks(@Param("projId") Long projId);
}
