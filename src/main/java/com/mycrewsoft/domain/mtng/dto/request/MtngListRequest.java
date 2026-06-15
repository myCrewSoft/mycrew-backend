package com.mycrewsoft.domain.mtng.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class MtngListRequest {

    @Schema(description = "검색 키워드 (회의명, 회의록, 참여자명 대상)")
    private String keyword;

    @Schema(description = "조회 시작일시 (이 시각 이후 종료되는 회의)")
    private LocalDateTime beginDt;

    @Schema(description = "조회 종료일시 (이 시각 이전 시작되는 회의)")
    private LocalDateTime endDt;
}