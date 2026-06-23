package com.mycrewsoft.domain.search.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class TaskSearchDetails implements SearchResultDetails {
    private Long projectId;
    private String projectName;
    private String managerName;
    private String statusCode;
    private String statusName;
    private String priorityCode;
    private String priorityName;
    private LocalDate dueDate;
}
