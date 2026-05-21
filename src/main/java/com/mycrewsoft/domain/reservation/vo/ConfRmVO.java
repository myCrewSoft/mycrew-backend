package com.mycrewsoft.domain.reservation.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfRmVO {

	private Long ConfRmId;			// 회의실 ID - PK
	
	private String ConfRmNm;		// 회의실명
	private String ConfRmHo;		// 회의실 호수
	private Integer ConfRmFlr;		// 회의실 층수
	private Long ConfRmMngrId;		// 회의실 관리자 ID(TB_MEMBER)
	private String UseYn;			// 사용 여부
	private String ConfRmColor;		// 회의실 색상
}
