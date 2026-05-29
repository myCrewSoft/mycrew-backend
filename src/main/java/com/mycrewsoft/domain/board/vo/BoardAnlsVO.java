package com.mycrewsoft.domain.board.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardAnlsVO {

	private Long bbsAnlsRsltId; //게시판분석결과ID
	private String anlsSj; //분석제목
	private String anlsCn; //분석내용
	private Integer riskMesureVal; //위험도 측정값
	private Long boardId; //게시판ID
}
