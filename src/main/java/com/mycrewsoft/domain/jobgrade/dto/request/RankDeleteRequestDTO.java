package com.mycrewsoft.domain.jobgrade.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "직급 삭제 요청 DTO")
public class RankDeleteRequestDTO {
	@Schema(description = "대체 직급 ID. 삭제 시 해당 직급에 속한 직원들이 이 직급으로 이동됩니다.", example = "RANK_02")
    private String replacementRankId;
}
