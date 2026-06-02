package com.mycrewsoft.domain.jobgrade.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "직급 수정 요청 DTO")
public class RankUpdateRequestDTO {

    @NotBlank
    @Schema(description = "직급 이름", example = "사원")
    private String rankName;

    @NotNull
    @Min(0)
    @Schema(description = "직급 순서 (낮을수록 높은 직급)", example = "0")
    private Integer sortOrder;
}
