package com.mycrewsoft.domain.projectmember.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProjectMemberVO {
	private Long empId;				//프로젝트 참여자 아이디
	private Long projId;			//프로젝트 아이디
	private LocalDateTime joinDt;	//프로젝트 참여일시
	private LocalDateTime leaveDt;	//프로젝트 퇴출일시
	
	//조회용
	private String empNm;
    private String deptNm;
}
