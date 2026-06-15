package com.mycrewsoft.domain.board.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter 
public class BoardLikeVo {

	private Long boardId; 
	private Long empId; 
	private LocalDateTime frstRegDt; 

}