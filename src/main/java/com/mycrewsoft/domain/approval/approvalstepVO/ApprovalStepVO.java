package com.mycrewsoft.domain.approval.approvalstepVO;

import java.time.LocalDateTime;
import java.util.List;

import com.mycrewsoft.domain.approval.approvallineVO.ApprovalLineVO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalStepVO {
	private Long aprvlStepSn;
	private Long drftDocSn;
	private Long empId;
	private String aprvlMthdCd;
	private Long reqAprvlCnt;
	private String stepPrgrsCd;
	private Long frstRgtrId;
	private LocalDateTime frstRegDt;
	private Long lastMdfrId;
	private LocalDateTime lastMdfcnDt;
	private Long aprvlOrd;
	
	private List<ApprovalLineVO> approvalLines;
}
