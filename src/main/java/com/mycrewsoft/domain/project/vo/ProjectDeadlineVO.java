package com.mycrewsoft.domain.project.vo;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectDeadlineVO {
    private Long projId;
    private String projNm;
    private List<Long> empIds;
}