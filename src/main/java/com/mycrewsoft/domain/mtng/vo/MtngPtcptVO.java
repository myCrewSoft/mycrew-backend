package com.mycrewsoft.domain.mtng.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class MtngPtcptVO {

    @Setter
    private Long mtngPtcptId; // 회의참여ID

    private Long mtngId; // 회의ID

    private Long empId; // 직원ID
}