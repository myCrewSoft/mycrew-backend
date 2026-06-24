package com.mycrewsoft.domain.task.scheduler;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mycrewsoft.domain.task.event.TaskDeadlineEvent;
import com.mycrewsoft.domain.task.mapper.TaskMapper;
import com.mycrewsoft.domain.task.vo.TaskDeadlineVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskDeadlineScheduler {

    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 9 * * *")  // 매일 오전 9시 실행
    public void notifyTaskDeadline() {
        List<TaskDeadlineVO> tasks = taskMapper.selectTasksDueTomorrow();

        for (TaskDeadlineVO task : tasks) {
            eventPublisher.publishEvent(
                new TaskDeadlineEvent(task.getTaskId(), task.getProjId(), task.getTaskNm(), task.getRcvrEmpIds())
            );
        }
    }
}
