package com.mycrewsoft.domain.approval.approvaltemplateVO;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.Builder.Default;

@Getter
@Setter
@Builder
public class ApprovalTemplateVO {
	private String tmplatCd;
	private String tmplatNm;
	private String tmplatCn;
	private String useYn;
	private Long frstRgtrId;
	private LocalDateTime frstRegDt;
	private Long lastMdfrId;
	private LocalDateTime lastMdfcnDt;
	private Long atchFileId;
}
