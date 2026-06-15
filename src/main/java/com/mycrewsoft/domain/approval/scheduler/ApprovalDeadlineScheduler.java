package com.mycrewsoft.domain.approval.scheduler;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mycrewsoft.domain.approval.event.ApprovalDeadlineEvent;
import com.mycrewsoft.domain.approval.mapper.ApprovalDraftMapper;
import com.mycrewsoft.domain.approval.vo.ApprovalDeadlineVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalDeadlineScheduler {

    private final ApprovalDraftMapper approvalDraftMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 9 * * *")
    public void notifyApprovalDeadline() {
        List<ApprovalDeadlineVO> list = approvalDraftMapper.selectApprovalsDueSoon();

        for (ApprovalDeadlineVO vo : list) {
            eventPublisher.publishEvent(
                new ApprovalDeadlineEvent(vo.getDocTtl(), vo.getAprvrEmpId())
            );
        }
    }
}