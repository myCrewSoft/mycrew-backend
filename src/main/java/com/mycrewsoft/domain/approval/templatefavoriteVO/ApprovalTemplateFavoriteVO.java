package com.mycrewsoft.domain.approval.templatefavoriteVO;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalTemplateFavoriteVO {
	private Long empId;
	private String tmplatCd;
	private LocalDateTime frstRegDt;
}
