package com.mycrewsoft.domain.project.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.mycrewsoft.domain.notification.vo.AlrmRcvrVO;
import com.mycrewsoft.domain.projectmember.vo.ProjectMemberVO;
import com.mycrewsoft.domain.projectreport.vo.ProjectReportVO;
import com.mycrewsoft.domain.task.vo.TaskVO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectVO {
	private Long projAtchFileId;
	private String projNm;
	private String projBgngYmd;
	private String projEndYmd;
	private String projStatCd;
	private LocalDate projCreatDt;
	private LocalDate projMdfcnDt;
	private LocalDate projStatChgDt;
	private Long projLdrMbrId;
	private Long projId;
	private Long chtrmId;
	private Long projMgrMbrId;
	
	// Has
	private List<TaskVO> taskList;	
	private List<ProjectMemberVO> projMemberList;
	private List<ProjectReportVO> projReportList;
}
