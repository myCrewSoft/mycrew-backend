package com.mycrewsoft.domain.search.dto.response;

import com.mycrewsoft.domain.search.enums.SearchType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "통합 검색 응답 DTO")
@Getter
@Builder
public class SearchResponse {

    @Schema(description = "검색 결과 PK", example = "1")
    private Long id;

    @Schema(description = "상위 도메인 PK (TASK일 경우 projectId, 나머지 null)", example = "3")
    private Long parentId;

    @Schema(description = "검색 결과 도메인 타입", example = "TASK")
    private SearchType type;

    @Schema(description = "검색 결과 제목", example = "API 설계 완료")
    private String title;

    @Schema(description = "도메인 구분 보조 텍스트", example = "업무")
    private String description;

    @Schema(description = "상태 뱃지 텍스트", example = "진행중")
    private String badgeText;
}