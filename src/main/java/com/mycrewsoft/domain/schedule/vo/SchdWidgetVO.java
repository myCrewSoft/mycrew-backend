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
    private LocalDateTime start;
    private LocalDateTime end;
    private Boolean allDay;
    private String deptNm;
    private String projNm;
    private String taskNm;
}