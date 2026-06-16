package com.mycrewsoft.domain.mtng.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MtngUpdateRequest {

    @Schema(description = "회의명")
    private String mtngNm;

    @Schema(description = "회의 진행방식 코드 (변경 가능)")
    private String mtngTypeCd;

    @Schema(description = "시작일시")
    private LocalDateTime beginDt;

    @Schema(description = "종료일시")
    private LocalDateTime endDt;

    @Schema(description = "회의실ID, 사용 안 하면 null")
    private Long confRmId;

    @Schema(description = "참여자 직원ID 목록 (작성자 본인 자동 포함)")
    private List<Long> ptcptEmpIds;
}