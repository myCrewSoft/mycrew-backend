package com.mycrewsoft.domain.jobgrade.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RankRevokeRequestDTO {

    @NotEmpty
    private List<Long> empIds;
}
