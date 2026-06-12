package com.mycrewsoft.domain.video.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoPtcptDetailVO {

    private Long vconfPtcptId;
    private Long vconfId;
    private Long empId;
    private LocalDateTime joinDt;
    private LocalDateTime leavDt;

    private String empNm;
    private String deptNm;
    private String jbgdNm;
}