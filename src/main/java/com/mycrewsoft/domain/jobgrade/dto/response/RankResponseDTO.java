package com.mycrewsoft.domain.jobgrade.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RankResponseDTO {

    private String rankId;
    private String rankName;
    private Integer sortOrder;
    private String useYn;
    private Integer assignedEmployeeCount;
}
