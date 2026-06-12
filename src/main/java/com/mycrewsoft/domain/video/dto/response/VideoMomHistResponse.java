package com.mycrewsoft.domain.video.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "회의록 수정 이력 응답 DTO")
@Getter
@Setter
public class VideoMomHistResponse {

    @Schema(description = "수정 이력 ID")
    private Long histId;

    @Schema(description = "회의록 ID")
    private Long momId;

    @Schema(description = "수정된 회의록 내용")
    private String momCn;

    @Schema(description = "수정자 ID")
    private Long edtrId;

    @Schema(description = "수정 일시")
    private String editDt;
}