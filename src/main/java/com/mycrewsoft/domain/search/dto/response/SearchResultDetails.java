package com.mycrewsoft.domain.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** 검색 유형별 상세 응답이 공통으로 구현하는 마커 인터페이스입니다. */
@Schema(oneOf = {
        ProjectSearchDetails.class,
        TaskSearchDetails.class,
        ScheduleSearchDetails.class,
        MeetingSearchDetails.class,
        EducationSearchDetails.class,
        MailSearchDetails.class
})
public interface SearchResultDetails {
}
