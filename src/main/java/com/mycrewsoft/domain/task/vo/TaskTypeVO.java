package com.mycrewsoft.domain.task.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskTypeVO {
	 
	private String taskTypeCd;		// 업무 타입 코드
 
    private String taskTypeNm;		// 업무 타입 이름
    private String taskTypeExpln;	// 업무 타입 설명
    private String useYn;			// 타입 사용여부
}
 
