package com.mycrewsoft.domain.jobgrade.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "직급에 직원 할당 요청 DTO")
public class RankAssignRequestDTO {

    @NotEmpty
    @Schema(description = "할당할 직원들의 ID 리스트", example = "[1, 2, 3]")
    private List<Long> empIds;
}
