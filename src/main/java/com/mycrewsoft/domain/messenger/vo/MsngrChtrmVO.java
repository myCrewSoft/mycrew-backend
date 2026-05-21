package com.mycrewsoft.domain.messenger.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsngrChtrmVO {
	private Long ChtrmId;		// 채팅방 ID	- PK
	
	private String ChtrmNm;		// 채팅방 이름
	private String ChtrmExpln;	// 채팅방 설명
	private String ChtrmTypeCd;	// 채팅방 타입 코드
	private Long EstblshId;		// 개설자 ID(TB_MEMBER)
	
	private LocalDateTime CreatDt;		// 개설일시
	private LocalDateTime EndDt;			// 종료일시
	
	private List<MsngrChtrmPtcptVO> MsngrChtrmPtcpt; // 참여자 목록
	private List<MsngrMsgVO> MsngrMsg; // 메시지 목록
	
}
