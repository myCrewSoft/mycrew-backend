package com.mycrewsoft.domain.task.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DateUtil;
import com.mycrewsoft.domain.projectmember.mapper.ProjectMemberMapper;
import com.mycrewsoft.domain.projectmember.vo.ProjectMemberVO;
import com.mycrewsoft.domain.task.dto.response.TaskDashboardResponse;
import com.mycrewsoft.domain.task.dto.response.TaskDashboardSummaryResponse;
import com.mycrewsoft.domain.task.dto.response.TaskUpcomingResponse;
import com.mycrewsoft.domain.task.mapper.TaskDashboardMapper;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskDashboardServiceImpl implements TaskDashboardService{

	private final TaskDashboardMapper dashboardMapper;
	private final ProjectMemberMapper projectMemberMapper;
	
	@Override
	@Transactional(readOnly = true)
	public TaskDashboardResponse getTaskDashboard(Long projId) {
		// 본인 currentEmpId 조회
        Long currentEmpId = getCurrentEmpIdOrThrow();

        // 해당 프로젝트의 참여자인지 확인
        List<ProjectMemberVO> projectMemberList =
                projectMemberMapper.selectProjectMemberList(projId);
        boolean isProjectMember = projectMemberList.stream()
                .anyMatch(member -> Objects.equals(member.getEmpId(), currentEmpId));
        if (!isProjectMember) throw new CustomException(ErrorCode.PROJECT_NOT_PARTICIPANT);
        
        // DB 조회
		TaskDashboardSummaryResponse summary = dashboardMapper.selectTaskDashboardSummary(projId);
        List<TaskUpcomingResponse> upcomingTasks = dashboardMapper.selectUpcomingTasks(projId);
        upcomingTasks.forEach(t -> t.setDDay(DateUtil.dDay(t.getTaskEndDt())));
        
        TaskDashboardResponse response = new TaskDashboardResponse();
        response.setSummary(summary);
        response.setUpcomingTasks(upcomingTasks);

        return response;
	}

    private Long getCurrentEmpIdOrThrow() {
        Long empId = SecurityUtil.getCurrentEmpId();
        if (empId == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return empId;
    }
}
