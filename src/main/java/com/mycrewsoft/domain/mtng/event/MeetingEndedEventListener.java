package com.mycrewsoft.domain.mtng.event;

import com.mycrewsoft.domain.mtng.service.MtngMomAiService;
import com.mycrewsoft.domain.mtng.service.MtngMomService;
import com.mycrewsoft.domain.mtng.vo.MtngDetailVO;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptDetailVO;
import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.video.mapper.VideoConfMapper;
import com.mycrewsoft.domain.video.vo.VideoChatLogVO;
import com.mycrewsoft.ai.chatbot.service.PromptService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEndedEventListener {

    private final MtngMomService mtngMomService;
    private final MtngMomAiService mtngMomAiService;

    // endConf 트랜잭션 커밋 후 별도 스레드에서 AI 회의록 생성
    // 메인 트랜잭션과 분리되어 타임아웃/실패가 회의 종료에 영향 없음
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingEnded(MeetingEndedEvent event) {
        Long vconfId = event.getVconfId();
        Long mtngId = event.getMtngId();

        try {
            String draftCn = mtngMomAiService.generateDraft(vconfId, mtngId);
            mtngMomService.createAiDraftMom(mtngId, draftCn);
            log.info("[회의록 AI 생성 완료] mtngId: {}", mtngId);

        } catch (Exception e) {
            log.error("[회의록 AI 생성 실패] vconfId: {}, mtngId: {}", vconfId, mtngId, e);
            try {
                mtngMomService.createAiDraftMom(mtngId, "");
            } catch (Exception ex) {
                log.error("[회의록 빈 초안 저장 실패] mtngId: {}", mtngId, ex);
            }
        }
    }

}