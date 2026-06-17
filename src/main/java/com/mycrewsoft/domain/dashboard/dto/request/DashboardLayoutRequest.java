package com.mycrewsoft.domain.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "대시보드 레이아웃 저장 요청")
public class DashboardLayoutRequest {

    @NotBlank
    @Schema(description = "레이아웃 JSON 문자열", example = "{\"widgets\":[{\"key\":\"attendance\",\"x\":0,\"y\":0,\"w\":4,\"h\":3}]}")
    private String lytJsonCn;
}