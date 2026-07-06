package com.mycrewsoft.domain.search.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class MeetingSearchDetails implements SearchResultDetails {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String hostName;
    private String statusCode;
    private String statusName;
    private Integer participantCount;
    private Boolean online;
    private String meetingTypeCode;
    private String meetingTypeName;
}
