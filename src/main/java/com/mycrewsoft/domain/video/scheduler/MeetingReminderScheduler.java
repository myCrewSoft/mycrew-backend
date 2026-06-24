package com.mycrewsoft.domain.video.scheduler;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mycrewsoft.domain.mtng.event.MeetingReminderEvent;
import com.mycrewsoft.domain.video.mapper.VideoConfMapper;
import com.mycrewsoft.domain.video.vo.MeetingReminderVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingReminderScheduler {

    private final VideoConfMapper videoConfMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 * * * * *")
    public void notifyMeetingReminder() {
        List<MeetingReminderVO> list = videoConfMapper.selectConfsStartingSoon();

        for (MeetingReminderVO vo : list) {
            eventPublisher.publishEvent(
                new MeetingReminderEvent(vo.getMeetingId(), vo.getVconfNm(), vo.getEmpIds())
            );
            log.info("[MeetingReminderScheduler] 회의 시작 10분 전 알림 - 회의: {}", vo.getVconfNm());
        }
    }
}
