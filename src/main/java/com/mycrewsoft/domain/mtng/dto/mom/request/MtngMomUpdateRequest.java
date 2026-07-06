package com.mycrewsoft.domain.mtng.dto.mom.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MtngMomUpdateRequest {

    @Schema(description = "회의록 내용")
    private String momCn;
}