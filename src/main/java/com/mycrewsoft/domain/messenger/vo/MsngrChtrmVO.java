package com.mycrewsoft.domain.messenger.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsngrChtrmVO {
	private Long chtrmId;		// 채팅방 ID	- PK
	
	private String chtrmNm;		// 채팅방 이름
	private String chtrmExpln;	// 채팅방 설명
	private String chtrmTypeCd;	// 채팅방 타입 코드
	private Long estblshId;		// 개설자 ID(TB_MEMBER)
	private Long chtrmImgAtchFileId;	// 채팅방 이미지 첨부파일 ID
	
	private LocalDateTime creatDt;		// 개설일시
	private LocalDateTime endDt;			// 종료일시
	private Integer participantCount;	// 현재 참여자 수 - DB 컬럼 아님
	
	private List<MsngrChtrmPtcptVO> msngrChtrmPtcpt; // 참여자 목록
	
}
