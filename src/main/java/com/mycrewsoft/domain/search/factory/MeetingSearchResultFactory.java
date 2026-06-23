package com.mycrewsoft.domain.search.factory;

import com.mycrewsoft.domain.search.dto.response.MeetingSearchDetails;
import com.mycrewsoft.domain.search.dto.response.SearchResultDetails;
import com.mycrewsoft.domain.search.enums.SearchType;
import com.mycrewsoft.domain.search.vo.SearchVO;
import org.springframework.stereotype.Component;

@Component
public class MeetingSearchResultFactory extends SearchResultFactory {
    @Override
    public SearchType getSupportedType() {
        return SearchType.MEETING;
    }

    @Override
    protected String createUrl(SearchVO vo) {
        return "/meeting/" + vo.getId();
    }

    @Override
    protected SearchResultDetails createDetails(SearchVO vo) {
        return MeetingSearchDetails.builder()
                .startDateTime(vo.getStartDateTime())
                .endDateTime(vo.getEndDateTime())
                .hostName(vo.getHostName())
                .statusCode(vo.getStatusCode())
                .statusName(firstNonNull(vo.getStatusName(), getStatusName(vo.getStatusCode())))
                .participantCount(vo.getParticipantCount())
                .online(vo.getMeetingOnline())
                .meetingTypeCode(vo.getMeetingTypeCode())
                .meetingTypeName(firstNonNull(
                        vo.getMeetingTypeName(), getMeetingTypeName(vo.getMeetingTypeCode())))
                .build();
    }

    private String getStatusName(String code) {
        return switch (firstNonNull(code, "")) {
            case "SCHEDULED" -> "예정";
            case "LIVE" -> "진행 중";
            case "ENDED" -> "종료";
            default -> null;
        };
    }

    private String getMeetingTypeName(String code) {
        return switch (firstNonNull(code, "")) {
            case "01" -> "온라인";
            case "02" -> "오프라인";
            case "03" -> "혼합";
            default -> null;
        };
    }
}
