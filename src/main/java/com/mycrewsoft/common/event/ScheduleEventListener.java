package com.mycrewsoft.common.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.mycrewsoft.domain.mtng.event.MeetingInvitedEvent;
import com.mycrewsoft.domain.project.event.ProjectCreatedEvent;
import com.mycrewsoft.domain.schedule.dto.command.MeetingScheduleCreateCommand;
import com.mycrewsoft.domain.schedule.dto.command.ProjectScheduleCreateCommand;
import com.mycrewsoft.domain.schedule.dto.command.TaskScheduleCreateCommand;
import com.mycrewsoft.domain.schedule.service.ScheduleService;
import com.mycrewsoft.domain.task.event.TaskAssignedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleEventListener {

    private final ScheduleService scheduleService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectCreated(ProjectCreatedEvent event) {
        try {
            scheduleService.createProjectSchedule(
                ProjectScheduleCreateCommand.builder()
                    .projId(event.getProjId())
                    .projNm(event.getProjNm())
                    .projBgngYmd(event.getProjBgngYmd())
                    .projEndYmd(event.getProjEndYmd())
                    .crtrId(event.getCrtrId())
                    .build()
            );
        } catch (Exception e) {
            log.error(
                "프로젝트 일정 자동 생성 실패. projId={}, projNm={}",
                event.getProjId(),
                event.getProjNm(),
                e
            );
        }
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssigned(TaskAssignedEvent event) {
        try {
            scheduleService.createTaskSchedule(
                TaskScheduleCreateCommand.builder()
                    .taskId(event.getTaskId())
                    .taskNm(event.getTaskNm())
                    .taskBgngYmd(event.getTaskBgngYmd())
                    .taskEndYmd(event.getTaskEndYmd())
                    .crtrId(event.getSchdWrtrId())
                    .build()
            );
        } catch (Exception e) {
            log.error(
                "업무 일정 자동 생성 실패. taskId={}, taskNm={}",
                event.getTaskId(),
                event.getTaskNm(),
                e
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingInvited(MeetingInvitedEvent event) {
        try {
            scheduleService.createMeetingSchedule(
                MeetingScheduleCreateCommand.builder()
                    .meetingId(event.getMeetingId())
                    .meetingNm(event.getMeetingNm())
                    .beginDt(event.getBeginDt())
                    .endDt(event.getEndDt())
                    .crtrId(event.getSchdWrtrId())
                    .build()
            );
        } catch (Exception e) {
            log.error(
                "회의 일정 자동 생성 실패. meetingId={}, meetingNm={}",
                event.getMeetingId(),
                event.getMeetingNm(),
                e
            );
        }
    }
}