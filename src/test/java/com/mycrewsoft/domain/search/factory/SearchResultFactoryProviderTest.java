package com.mycrewsoft.domain.search.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mycrewsoft.domain.search.dto.response.SearchResponse;
import com.mycrewsoft.domain.search.dto.response.TaskSearchDetails;
import com.mycrewsoft.domain.search.enums.SearchType;
import com.mycrewsoft.domain.search.vo.SearchVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearchResultFactoryProviderTest {

    @Test
    @DisplayName("업무 검색 결과를 업무 상세 DTO와 프로젝트 이동 URL로 생성한다")
    void createTaskSearchResult() {
        SearchResultFactoryProvider provider = new SearchResultFactoryProvider(List.of(
                new TaskSearchResultFactory()
        ));

        SearchVO vo = new SearchVO();
        vo.setId(15L);
        vo.setParentId(3L);
        vo.setType("TASK");
        vo.setTitle("로그인 API 구현");
        vo.setSummary("JWT 기반 로그인 기능을 구현합니다.");
        vo.setProjectName("MyCrew 개발");
        vo.setManagerName("남바완");
        vo.setStatusCode("01");
        vo.setStatusName("진행 중");
        vo.setPriorityCode("02");
        vo.setPriorityName("보통");
        vo.setDueDate(LocalDate.of(2026, 6, 25));

        SearchResponse response = provider.create(vo);

        assertThat(response.getType()).isEqualTo(SearchType.TASK);
        assertThat(response.getUrl()).isEqualTo("/project/3?tab=tasks&taskId=15");
        assertThat(response.getDetails()).isInstanceOf(TaskSearchDetails.class);

        TaskSearchDetails details = (TaskSearchDetails) response.getDetails();
        assertThat(details.getProjectId()).isEqualTo(3L);
        assertThat(details.getStatusCode()).isEqualTo("01");
        assertThat(details.getStatusName()).isEqualTo("진행 중");
        assertThat(details.getDueDate()).isEqualTo(LocalDate.of(2026, 6, 25));
    }

    @Test
    @DisplayName("날짜는 화면 표시 문자열이 아닌 ISO 형식으로 직렬화한다")
    void serializeDateAsIsoFormat() throws Exception {
        SearchResultFactoryProvider provider = new SearchResultFactoryProvider(List.of(
                new TaskSearchResultFactory()
        ));
        SearchVO vo = new SearchVO();
        vo.setId(15L);
        vo.setParentId(3L);
        vo.setType("TASK");
        vo.setTitle("로그인 API 구현");
        vo.setDueDate(LocalDate.of(2026, 6, 25));

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        String json = objectMapper.writeValueAsString(provider.create(vo));

        assertThat(json).contains("\"dueDate\":\"2026-06-25\"");
        assertThat(json).doesNotContain("2026.06.25");
    }
}
