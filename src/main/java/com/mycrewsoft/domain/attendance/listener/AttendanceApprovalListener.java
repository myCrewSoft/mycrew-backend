package com.mycrewsoft.domain.attendance.listener;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

import com.mycrewsoft.domain.approval.event.ApprovalApprovedEvent;
import com.mycrewsoft.domain.attendance.service.AttendanceLeaveService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 전자결재 최종 승인 이벤트를 받아 근태로 취합한다.
 * 해당 문서가 휴가/초과근무 신청과 연결된 경우에만 반영되고, 그 외 문서는 무시된다.
 * 원 트랜잭션 커밋 이후(AFTER_COMMIT)에 별도 트랜잭션으로 처리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceApprovalListener {

	private final AttendanceLeaveService attendanceLeaveService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onApprovalApproved(ApprovalApprovedEvent event) {
		if (event.getDrftDocSn() == null) {
			return;
		}
		try {
			attendanceLeaveService.reflectApprovedDocument(event.getDrftDocSn());
		} catch (Exception e) {
			// 취합 실패가 결재 자체를 되돌리지 않도록 로깅만 한다.
			log.error("근태 취합 실패 - drftDocSn={}", event.getDrftDocSn(), e);
		}
	}
}
