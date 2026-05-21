package com.mycrewsoft.domain.reservation.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfRmVO {

	private Long confRmId;			// 회의실 ID - PK
	
	private String confRmNm;		// 회의실명
	private String confRmHo;		// 회의실 호수
	private Integer confRmFlr;		// 회의실 층수
	private Long confRmMngrId;		// 회의실 관리자 ID(TB_MEMBER)
	private String useYn;			// 사용 여부
	private String confRmColor;		// 회의실 색상
}
