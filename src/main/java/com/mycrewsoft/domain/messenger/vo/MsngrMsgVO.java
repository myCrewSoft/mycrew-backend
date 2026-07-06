package com.mycrewsoft.domain.messenger.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsngrMsgVO {
	private Long msgId;				// 메시지 ID - PK
	private Long chtrmId;			// 채팅방 ID(MSNGR_CHTRM) - FK
	
	private Long sndrId;			// 발신자 ID(TB_MEMBER) - 논리 FK
	
	private String msgCn;			// 발신 내용
	
	private LocalDateTime creatDt;	// 발신 일시
}
