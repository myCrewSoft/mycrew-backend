package com.mycrewsoft.domain.project.scheduler;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mycrewsoft.domain.project.event.ProjectDeadlineEvent;
import com.mycrewsoft.domain.project.mapper.ProjectMapper;
import com.mycrewsoft.domain.project.vo.ProjectDeadlineVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectDeadlineScheduler {

    private final ProjectMapper projectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 9 * * *")
    public void notifyProjectDeadline() {
        List<ProjectDeadlineVO> list = projectMapper.selectProjectsDueTomorrow();

        for (ProjectDeadlineVO vo : list) {
            eventPublisher.publishEvent(
                new ProjectDeadlineEvent(vo.getProjNm(), vo.getEmpIds())
            );
            log.info("[ProjectDeadlineScheduler] 마감 알림 발송 - 프로젝트: {}", vo.getProjNm());
        }
    }
}