package com.mycrewsoft.domain.approval.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalDeadlineVO {
    private Long drftDocSn;
    private String docTtl;
    private Long aprvrEmpId;
}