package com.mycrewsoft.domain.mtng.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminMtngListPageResponse {

    @Schema(description = "회의 목록")
    private List<AdminMtngListResponse> meetings;

    @Schema(description = "전체 건수")
    private int totalCount;

    @Schema(description = "현재 페이지 번호")
    private int currentPage;

    @Schema(description = "전체 페이지 수")
    private int totalPages;
}