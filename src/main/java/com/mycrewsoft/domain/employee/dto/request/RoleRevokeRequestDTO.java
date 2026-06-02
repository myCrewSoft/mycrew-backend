package com.mycrewsoft.domain.employee.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleRevokeRequestDTO {

    @NotEmpty
    private List<Long> empIds;

    private String scopeTypeCd;

    private String scopeId;
}
