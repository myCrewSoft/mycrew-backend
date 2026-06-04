package com.mycrewsoft.domain.projectreport.vo;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProjectReportVO {
	private Long reportId;
	private Long projId;
	private String reportSj;
	private String reportCn;
	private LocalDate reportRegDt;
}
