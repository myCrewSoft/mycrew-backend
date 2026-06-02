package com.mycrewsoft.domain.projectmember.vo;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectMemberVO {
	private Long empId;
	private Long projId;
	private LocalDate joinDt;
	private LocalDate leaveDt;
}
