package com.mycrewsoft.domain.search.vo;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 통합 검색 UNION 쿼리의 유형별 컬럼을 담는 조회 전용 VO입니다. */
@Getter
@Setter
public class SearchVO {
    private Long id;
    private Long parentId;
    private String type;
    private String title;
    private String summary;

    private String statusCode;
    private String statusName;
    private String managerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer progress;
    private String projectStartDateText;
    private String projectEndDateText;

    private String projectName;
    private String priorityCode;
    private String priorityName;
    private LocalDate dueDate;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Boolean allDay;
    private String writerName;
    private String classificationCode;
    private String classificationName;

    private String hostName;
    private Integer participantCount;
    private Boolean meetingOnline;
    private String meetingTypeCode;
    private String meetingTypeName;

    private String instructorName;
    private String educationStartDate;
    private String deliveryMethod;
    private String location;

    private String senderName;
    private String senderAddress;
    private LocalDateTime receivedAt;
    private Boolean mailRead;
    private Boolean hasAttachment;
    private String bodyPreview;
}
