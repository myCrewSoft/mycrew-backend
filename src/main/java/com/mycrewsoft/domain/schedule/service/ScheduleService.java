package com.mycrewsoft.domain.schedule.service;

import java.time.LocalDateTime;
import java.util.List;

import com.mycrewsoft.domain.schedule.dto.command.MeetingScheduleCreateCommand;
import com.mycrewsoft.domain.schedule.dto.command.ProjectScheduleCreateCommand;
import com.mycrewsoft.domain.schedule.dto.command.TaskScheduleCreateCommand;
import com.mycrewsoft.domain.schedule.dto.request.ScheduleRequestDto;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleResponseDto;

public interface ScheduleService {
	
	void modifySchd(Long schdId, ScheduleRequestDto dto);
	
	void deleteSchd(Long schdId);
	
	ScheduleResponseDto readSchd(Long schdId);
	
	List<ScheduleResponseDto> readSchdList(LocalDateTime beginDt, LocalDateTime endDt);
	
	Long createSchd(ScheduleRequestDto dto); // 사용자가 직접 등록

	Long createProjectSchedule(ProjectScheduleCreateCommand command); // 프로젝트 자동 일정

	Long createTaskSchedule(TaskScheduleCreateCommand command); // 업무 자동 일정

	Long createMeetingSchedule(MeetingScheduleCreateCommand command); // 회의 자동 일정
}
