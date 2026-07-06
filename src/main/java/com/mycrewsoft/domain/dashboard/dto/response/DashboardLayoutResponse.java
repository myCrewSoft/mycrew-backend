package com.mycrewsoft.domain.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "대시보드 레이아웃 응답")
public class DashboardLayoutResponse {

    @Schema(description = "대시보드 ID")
    private Long dshbdLytId;

    @Schema(description = "사원 ID")
    private Long empId;

    @Schema(description = "레이아웃 JSON 문자열")
    private String lytJsonCn;

    @Schema(description = "최종 수정일시")
    private LocalDateTime lastMdfcnDt;
}