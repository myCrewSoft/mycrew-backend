package com.mycrewsoft.domain.schedule.service;

import java.time.LocalDateTime;
import java.util.List;

import com.mycrewsoft.domain.schedule.dto.request.ScheduleRequestDto;
import com.mycrewsoft.domain.schedule.dto.response.ScheduleResponseDto;
import com.mycrewsoft.domain.schedule.vo.SchdSearchVO;

public interface ScheduleService {

	Long createSchd(ScheduleRequestDto dto);
	
	void modifySchd(Long schdId, ScheduleRequestDto dto, Long empId);
	
	void deleteSchd(Long schdId, Long empId);
	
	ScheduleResponseDto readSchd(Long schdId);
	
	List<ScheduleResponseDto> readSchdList(LocalDateTime beginDt, LocalDateTime endDt);
}
