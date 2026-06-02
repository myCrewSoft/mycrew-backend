package com.mycrewsoft.domain.jobgrade.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "직급에서 직원 할당 해제 요청 DTO")
public class RankRevokeRequestDTO {

    @NotEmpty
    @Schema(description = "할당 해제할 직원들의 ID 리스트", example = "[1, 2, 3]")
    private List<Long> empIds;
}
