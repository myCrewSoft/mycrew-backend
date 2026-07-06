package com.mycrewsoft.domain.messenger.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsngrChtrmPtcptVO {
	private Long chtrmId;			// 채팅방 ID(MSNGR_CHTRM) - PK, FK
	
	private Long empId;				// 참여자 ID(TB_MEMBER) - PK, FK
	private String ptcptSttusCd;	// 참여자 상태(로그인, 비로그인, 자리비움)
	private Long lastCfmtnMsgId;	// 마지막 확인 메시지(MSNGR_MSG)
	
	private LocalDateTime joinDt;	// 참여 일시
	private LocalDateTime leavDt;	// 퇴장 일시
}
