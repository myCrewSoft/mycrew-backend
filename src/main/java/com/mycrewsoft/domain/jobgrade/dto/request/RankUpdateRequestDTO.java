package com.mycrewsoft.domain.jobgrade.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RankUpdateRequestDTO {

    @NotBlank
    private String rankName;

    @NotNull
    @Min(0)
    private Integer sortOrder;
}
