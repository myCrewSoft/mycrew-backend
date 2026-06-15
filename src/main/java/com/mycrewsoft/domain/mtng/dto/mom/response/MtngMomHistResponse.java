package com.mycrewsoft.domain.mtng.dto.mom.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MtngMomHistResponse {

    @Schema(description = "수정이력ID")
    private Long histId;

    @Schema(description = "수정 당시 내용")
    private String momCn;

    @Schema(description = "수정자ID")
    private Long edtrId;

    @Schema(description = "수정일시")
    private LocalDateTime editDt;

    @Schema(description = "수정자명")
    private String edtrNm;
}