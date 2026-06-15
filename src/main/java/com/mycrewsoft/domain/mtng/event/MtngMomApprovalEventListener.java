package com.mycrewsoft.domain.mtng.event;

import com.mycrewsoft.domain.approval.event.ApprovalApprovedEvent;
import com.mycrewsoft.domain.mtng.mapper.MtngMomMapper;
import com.mycrewsoft.domain.mtng.vo.mom.MtngMomVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MtngMomApprovalEventListener {

    private final MtngMomMapper mtngMomMapper;

    // 전자결재 최종 승인 완료 시 회의록 상태를 04(확정)으로 자동 업데이트
    // @Async로 전자결재 트랜잭션과 분리 — 리스너 실패가 결재 완료에 영향 없음
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalApproved(ApprovalApprovedEvent event) {
        Long drftDocSn = event.getDrftDocSn();
        if (drftDocSn == null) {
            return;
        }

        MtngMomVO momVO = mtngMomMapper.selectMomByDrftDocSn(drftDocSn);
        if (momVO == null) {
            // 회의록과 무관한 다른 도메인의 결재 (근태 취합 등) → 무시
            return;
        }

        mtngMomMapper.updateMtngMomSttus(momVO.getMomId(), "04");
        log.info("[회의록 결재 완료] momId: {}, drftDocSn: {}", momVO.getMomId(), drftDocSn);
    }
}