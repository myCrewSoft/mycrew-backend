package com.mycrewsoft.domain.search.factory;

import com.mycrewsoft.domain.search.dto.response.ProjectSearchDetails;
import com.mycrewsoft.domain.search.dto.response.SearchResultDetails;
import com.mycrewsoft.domain.search.enums.SearchType;
import com.mycrewsoft.domain.search.vo.SearchVO;
import org.springframework.stereotype.Component;

@Component
public class ProjectSearchResultFactory extends SearchResultFactory {
    @Override
    public SearchType getSupportedType() {
        return SearchType.PROJECT;
    }

    @Override
    protected String createUrl(SearchVO vo) {
        return "/project/" + vo.getId();
    }

    @Override
    protected SearchResultDetails createDetails(SearchVO vo) {
        String statusCode = vo.getStatusCode();
        return ProjectSearchDetails.builder()
                .statusCode(statusCode)
                .statusName(firstNonNull(vo.getStatusName(), getStatusName(statusCode)))
                .managerName(vo.getManagerName())
                .startDate(vo.getStartDate() != null
                        ? vo.getStartDate() : SearchDateParser.parse(vo.getProjectStartDateText()))
                .endDate(vo.getEndDate() != null
                        ? vo.getEndDate() : SearchDateParser.parse(vo.getProjectEndDateText()))
                .progress(vo.getProgress())
                .build();
    }

    private String getStatusName(String code) {
        return switch (firstNonNull(code, "")) {
            case "01" -> "예정";
            case "02" -> "진행 중";
            case "03" -> "완료";
            case "04" -> "중단";
            default -> null;
        };
    }
}
