package com.mycrewsoft.domain.search.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class ProjectSearchDetails implements SearchResultDetails {
    private String statusCode;
    private String statusName;
    private String managerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer progress;
}
