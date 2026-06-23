package com.mycrewsoft.domain.search.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ScheduleSearchDetails implements SearchResultDetails {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Boolean allDay;
    private String writerName;
    private String classificationCode;
    private String classificationName;
}
