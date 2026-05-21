package com.mycrewsoft.domain.messenger.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsngrMsgVO {
	private Long MsgId;		// 메시지 ID - PK
	
	private Long ChtrmId;	// 채팅방 ID(MSNGR_CHTRM)
	private String SndrId;	// 발신자 ID(TB_MEMBER)
	private Long MsgCn;		// 발신 내용
	private Long CreatDt;	// 발신 일시
}
