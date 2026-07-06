package com.mycrewsoft.ai.chatbot;

import java.time.LocalDateTime;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.mycrewsoft.domain.attendance.dto.response.AtndTodayResponse;
import com.mycrewsoft.domain.attendance.service.AttendanceService;
import com.mycrewsoft.domain.schedule.dto.request.ScheduleRequestDto;
import com.mycrewsoft.domain.schedule.service.ScheduleService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatbotTools {

    private final AttendanceService attendanceService;
    private final ScheduleService scheduleService;

    @Tool(description = "오늘 내 근태 현황을 조회합니다. 출근 시각, 퇴근 시각, 근태 상태를 반환합니다.")
    public AtndTodayResponse getMyTodayAttendance() {
        return attendanceService.getMyToday();
    }

    @Tool(description = """
        일정을 등록합니다.
        schdClsfCd: 일정 구분 코드 - C001(전사 공통 일정), C002(개인 일정)
        schdNm: 일정명
        beginDt: 시작일시 (ISO-8601 형식, 예: 2026-06-30T14:00:00)
        endDt: 종료일시 (ISO-8601 형식)
        allDayYn: 종일 여부 - 시간 없이 하루 종일이면 y, 특정 시간이 있으면 n
        schdDetailCn: 상세 내용 (없으면 null)
        """)
    public Long createSchedule(
            String schdClsfCd,
            String schdNm,
            LocalDateTime beginDt,
            LocalDateTime endDt,
            String allDayYn,
            String schdDetailCn) {

        ScheduleRequestDto dto = ScheduleRequestDto.builder()
                .schdClsfCd(schdClsfCd)
                .schdNm(schdNm)
                .beginDt(beginDt)
                .endDt(endDt)
                .allDayYn(allDayYn)
                .reptYn("n")
                .schdDetailCn(schdDetailCn)
                .build();
        return scheduleService.createSchd(dto);
    }

}
