package com.mycrewsoft.domain.search.factory;

import com.mycrewsoft.domain.search.dto.response.ScheduleSearchDetails;
import com.mycrewsoft.domain.search.dto.response.SearchResultDetails;
import com.mycrewsoft.domain.search.enums.SearchType;
import com.mycrewsoft.domain.search.vo.SearchVO;
import org.springframework.stereotype.Component;

@Component
public class ScheduleSearchResultFactory extends SearchResultFactory {
    @Override
    public SearchType getSupportedType() {
        return SearchType.SCHEDULE;
    }

    @Override
    protected String createUrl(SearchVO vo) {
        return "/schedule?schdId=" + vo.getId();
    }

    @Override
    protected SearchResultDetails createDetails(SearchVO vo) {
        return ScheduleSearchDetails.builder()
                .startDateTime(vo.getStartDateTime())
                .endDateTime(vo.getEndDateTime())
                .allDay(vo.getAllDay())
                .writerName(vo.getWriterName())
                .classificationCode(vo.getClassificationCode())
                .classificationName(firstNonNull(
                        vo.getClassificationName(), getClassificationName(vo.getClassificationCode())))
                .build();
    }

    private String getClassificationName(String code) {
        return switch (firstNonNull(code, "")) {
            case "C001" -> "전사 공통";
            case "C002" -> "개인";
            case "C003" -> "간부";
            case "C004" -> "부서";
            case "C005" -> "프로젝트";
            case "C006" -> "업무";
            case "C007" -> "회의";
            case "C008" -> "회의실 예약";
            default -> null;
        };
    }
}
