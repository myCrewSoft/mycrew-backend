package com.mycrewsoft.domain.task.service;

import com.mycrewsoft.domain.task.dto.response.TaskDashboardResponse;

public interface TaskDashboardService {

	TaskDashboardResponse getTaskDashboard(Long projId);
}
