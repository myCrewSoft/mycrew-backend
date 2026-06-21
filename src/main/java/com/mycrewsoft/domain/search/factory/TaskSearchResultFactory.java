package com.mycrewsoft.domain.search.factory;

import com.mycrewsoft.domain.search.dto.response.SearchResultDetails;
import com.mycrewsoft.domain.search.dto.response.TaskSearchDetails;
import com.mycrewsoft.domain.search.enums.SearchType;
import com.mycrewsoft.domain.search.vo.SearchVO;
import org.springframework.stereotype.Component;

@Component
public class TaskSearchResultFactory extends SearchResultFactory {
    @Override
    public SearchType getSupportedType() {
        return SearchType.TASK;
    }

    @Override
    protected String createUrl(SearchVO vo) {
        return "/project/" + vo.getParentId() + "?tab=tasks&taskId=" + vo.getId();
    }

    @Override
    protected SearchResultDetails createDetails(SearchVO vo) {
        String statusCode = vo.getStatusCode();
        return TaskSearchDetails.builder()
                .projectId(vo.getParentId())
                .projectName(vo.getProjectName())
                .managerName(vo.getManagerName())
                .statusCode(statusCode)
                .statusName(firstNonNull(vo.getStatusName(), getStatusName(statusCode)))
                .priorityCode(vo.getPriorityCode())
                .priorityName(firstNonNull(vo.getPriorityName(), getPriorityName(vo.getPriorityCode())))
                .dueDate(vo.getDueDate())
                .build();
    }

    private String getStatusName(String code) {
        return switch (firstNonNull(code, "")) {
            case "00" -> "할 일";
            case "01" -> "진행 중";
            case "02" -> "완료";
            case "03" -> "중단";
            default -> null;
        };
    }

    private String getPriorityName(String code) {
        return switch (firstNonNull(code, "")) {
            case "01" -> "높음";
            case "02" -> "보통";
            case "03" -> "낮음";
            default -> null;
        };
    }
}
