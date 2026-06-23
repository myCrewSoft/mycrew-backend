package com.mycrewsoft.domain.search.factory;

import com.mycrewsoft.domain.search.dto.response.EducationSearchDetails;
import com.mycrewsoft.domain.search.dto.response.SearchResultDetails;
import com.mycrewsoft.domain.search.enums.SearchType;
import com.mycrewsoft.domain.search.vo.SearchVO;
import org.springframework.stereotype.Component;

@Component
public class EducationSearchResultFactory extends SearchResultFactory {
    @Override
    public SearchType getSupportedType() {
        return SearchType.EDUCATION;
    }

    @Override
    protected String createUrl(SearchVO vo) {
        return "/education/" + vo.getId();
    }

    @Override
    protected SearchResultDetails createDetails(SearchVO vo) {
        return EducationSearchDetails.builder()
                .startDate(vo.getStartDate() != null
                        ? vo.getStartDate() : SearchDateParser.parse(vo.getEducationStartDate()))
                .endDate(vo.getEndDate())
                .instructorName(vo.getInstructorName())
                .statusCode(vo.getStatusCode())
                .statusName(vo.getStatusName())
                .deliveryMethod(vo.getDeliveryMethod())
                .location(vo.getLocation())
                .build();
    }

}
