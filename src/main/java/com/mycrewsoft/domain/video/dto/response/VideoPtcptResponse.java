package com.mycrewsoft.domain.video.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "화상회의 참여자 응답 DTO")
@Getter
@Setter
public class VideoPtcptResponse {

    @Schema(description = "참여자 PK")
    private Long vconfPtcptId;

    @Schema(description = "사원 ID")
    private Long empId;

    @Schema(description = "사원 이름")
    private String empNm;

    @Schema(description = "부서명")
    private String deptNm;

    @Schema(description = "직급명")
    private String jbgdNm;   
    
    @Schema(description = "참여 일시")
    private LocalDateTime joinDt;

    @Schema(description = "퇴장 일시")
    private LocalDateTime leavDt;
}