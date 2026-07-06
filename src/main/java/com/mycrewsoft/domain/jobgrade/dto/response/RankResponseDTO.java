package com.mycrewsoft.domain.jobgrade.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "직급 정보 응답 DTO")
public class RankResponseDTO {
	@Schema(description = "직급 ID", example = "JOB01")
    private String rankId;
	@Schema(description = "직급 이름", example = "사원")
    private String rankName;
	@Schema(description = "직급 순서 (낮을수록 높은 직급)", example = "0")
    private Integer sortOrder;
	@Schema(description = "사용 여부", example = "Y")
    private String useYn;
	@Schema(description = "직급에 할당된 직원 수", example = "5")
    private Integer assignedEmployeeCount;
}
