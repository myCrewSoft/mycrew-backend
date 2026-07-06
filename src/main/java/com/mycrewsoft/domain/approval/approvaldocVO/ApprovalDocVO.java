package com.mycrewsoft.domain.approval.approvaldocVO;

import java.time.LocalDateTime;
import java.util.List;

import com.mycrewsoft.domain.approval.approvalfileVO.ApprovalFileVO;
import com.mycrewsoft.domain.approval.approvalstepVO.ApprovalStepVO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalDocVO {
	private String docTtl;
	private LocalDateTime aprvlHopeDt;
	private LocalDateTime aprvlCmptnDt;
	private LocalDateTime rtrnDt;
	private Long drftDocSn;
	private String tmplatCd;
	private Long empId;
	private String aprvlFullRecCn;
	private String aprvlFullCn;
	private LocalDateTime drftReqstDt;
	private String aprvlDocSttsCd;
	
	private List<ApprovalFileVO> approvalFile;
	private ApprovalStepVO approvalSteps;
}
