package com.mycrewsoft.common.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.mycrewsoft.domain.messenger.service.MsngrService;
import com.mycrewsoft.domain.project.event.ProjectCreatedEvent;
import com.mycrewsoft.domain.project.service.ProjectService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessengerEventListener {

    private final MsngrService msngrService;
    private final ProjectService projectService;
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectCreated(ProjectCreatedEvent event) {
        try {
        	Long chtrmId = msngrService.createProjectChtrm(
                event.getProjId(),
                event.getProjNm(),
                event.getCrtrId(),
                event.getEmpIds()
            );
        	
        	projectService.updateProjectChtrmId(
                event.getProjId(),
                chtrmId
            );
    	
        } catch (Exception e) {
            log.error(
                "프로젝트 메신저 채팅방 자동 생성 실패. projId={}, projNm={}",
                event.getProjId(),
                event.getProjNm(),
                e
            );
        }
    }
}