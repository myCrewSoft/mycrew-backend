package com.mycrewsoft.domain.task.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDeadlineVO {
    private Long taskId;
    private Long projId;
    private String taskNm;
    private List<Long> rcvrEmpIds;
}
