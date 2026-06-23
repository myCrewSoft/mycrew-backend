package com.mycrewsoft.domain.search.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class EducationSearchDetails implements SearchResultDetails {
    private LocalDate startDate;
    private LocalDate endDate;
    private String instructorName;
    private String statusCode;
    private String statusName;
    private String deliveryMethod;
    private String location;
}
