package com.mycrewsoft.domain.mtng.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MtngPtcptResponse {

    @Schema(description = "직원ID")
    private Long empId;

    @Schema(description = "직원명")
    private String empNm;

    @Schema(description = "부서명")
    private String deptNm;

    @Schema(description = "직급명")
    private String jobGrdNm;

    @Schema(description = "프로필 이미지 파일ID", nullable = true)
    private Long prflImgFileId;
}