package com.mycrewsoft.domain.search.dto.response;

import com.mycrewsoft.domain.search.enums.SearchType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "통합 검색 결과 응답 DTO")
@Getter
@Builder
public class SearchResponse {

    @Schema(description = "검색 결과 PK", example = "15")
    private Long id;

    @Schema(description = "상위 항목 PK. 업무인 경우 프로젝트 ID", example = "3")
    private Long parentId;

    @Schema(description = "검색 결과 유형", example = "TASK")
    private SearchType type;

    @Schema(description = "검색 결과 제목", example = "로그인 API 구현")
    private String title;

    @Schema(description = "검색 결과 요약", example = "JWT 기반 로그인 기능을 구현합니다.")
    private String summary;

    @Schema(description = "상세 페이지 이동 주소", example = "/project/3?tab=tasks&taskId=15")
    private String url;

    @Schema(description = "검색 유형별 상세 데이터")
    private SearchResultDetails details;
}
