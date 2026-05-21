package com.mycrewsoft.domain.messenger.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsngrMsgVO {
	private Long MsgId;				// 메시지 ID - PK
	private Long ChtrmId;			// 채팅방 ID(MSNGR_CHTRM) - FK
	
	private Long SndrId;			// 발신자 ID(TB_MEMBER)
	
	private String MsgCn;			// 발신 내용
	
	private LocalDateTime CreatDt;	// 발신 일시
}
