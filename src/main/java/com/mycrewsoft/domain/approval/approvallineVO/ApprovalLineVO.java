package com.mycrewsoft.domain.approval.approvallineVO;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalLineVO {
	private Long aprvlStepSn;
	private Long empId;
	private Long aprvlLineSn;
	private Long drftDocSn;
	private Long aprvrEmpId;
	private LocalDateTime aprvlDt;
	private String aprvlPrgrsCd;
	private String aprvlRsn;
	private String rtrnRsn;
}
