package com.mycrewsoft.domain.schedule.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SchdWidgetVO {

    private Long id;
    private String scheduleTypeCode;
    private String title;
    private LocalDateTime startDt;
    private LocalDateTime endDt;
    private String allDayYn;
    private String deptNm;
    private String projNm;
    private String taskNm;
}