package com.mycrewsoft.domain.mtng.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminMtngPtcptResponse {

    @Schema(description = "참여자 사번")
    private Long empId;

    @Schema(description = "참여자명")
    private String empNm;

    @Schema(description = "부서명")
    private String deptNm;

    @Schema(description = "직급명")
    private String jbgdNm;

    @Schema(description = "참여 상태 코드 (PT001: 참여중 / PT002: 미입장 / PT003: 퇴장)")
    private String ptcptSttusCd;
}